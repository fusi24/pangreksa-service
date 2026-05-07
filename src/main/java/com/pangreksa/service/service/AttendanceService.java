package com.pangreksa.service.service;

import com.pangreksa.service.model.exception.AlreadyCheckedInException;
import com.pangreksa.service.model.exception.EntityNotFoundException;
import com.pangreksa.service.model.exception.NotCheckedInException;
import com.pangreksa.service.model.exception.WorkScheduleNotFoundException;
import com.pangreksa.service.shared.security.AppUserInfo;
import com.pangreksa.service.model.entity.*;
import com.pangreksa.service.model.enumerate.LeaveStatusEnum;
import com.pangreksa.service.model.repo.FwAppUserRepository;
import com.pangreksa.service.model.repo.HrAttendanceRepository;
import com.pangreksa.service.model.repo.HrCompanyBranchRepository;
import com.pangreksa.service.model.repo.HrLeaveApplicationRepository;
import com.pangreksa.service.model.repo.HrOfficeLocationRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.pangreksa.service.model.enumerate.WorkScheduleType;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private HrAttendanceRepository attendanceRepo;

    @Autowired
    private HrWorkScheduleService workScheduleService; // You'll implement this

    @Autowired
    private CalendarService calendarService; // For holidays

    @Autowired
    private FwAppUserRepository appUserRepository;

    @Autowired
    private HrLeaveApplicationRepository hrLeaveApplicationRepository;

    @Autowired
    private HrCompanyBranchRepository hrCompanyBranchRepository;

    @Autowired
    private HrOfficeLocationRepository officeLocationRepo;

    @Getter
    private FwAppUser currentUser;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    public void setUser(AppUserInfo user) {
        this.currentUser = findAppUserByUserId(user.getUserId().toString());
    }

    private FwAppUser findAppUserByUserId(String userId) {
        return appUserRepository.findByUsername(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }

    public boolean hasUnfinishedAttendanceBeforeToday() {
        LocalDate today = LocalDate.now(JAKARTA_ZONE);

        return attendanceRepo.findByAppUserIdAndAttendanceDateBetween(
                currentUser.getId(),
                today.minusDays(30), // safe window
                today.minusDays(1)
        ).stream().anyMatch(att ->
                att.getCheckIn() != null && att.getCheckOut() == null
        );
    }

    public HrAttendance getOrCreateTodayAttendance(FwAppUser user) {
        LocalDate today = LocalDate.now();
        return attendanceRepo.findByAppUserIdAndAttendanceDate(user.getId(), today)
                .orElseGet(() -> {
                    HrAttendance att = new HrAttendance();
                    att.setAppUser(user);
                    att.setPerson(user.getPerson()); // assuming AppUserInfo has getPerson()
                    HrWorkSchedule schedule = workScheduleService.getActiveScheduleForUser(user, today);
                    if (schedule == null) {
                        return null;
                    }
                    att.setWorkSchedule(schedule);
                    att.setAttendanceDate(today);
                    att.setStatus("ALPHA"); // temporary
                    return att;
                });
    }

    public boolean shouldShowCheckInPopup() {
        LocalDate today = LocalDate.now(JAKARTA_ZONE);

        if (!isWorkingDay(today)) return false;

        HrPerson employee = currentUser.getPerson();

        if (isOnApprovedLeave(today, employee)) return false;

        Optional<HrAttendance> todayAttendance =
                attendanceRepo.findByAppUserIdAndAttendanceDate(
                        currentUser.getId(), today
                );

        return todayAttendance.isEmpty();
    }


    public boolean shouldShowCheckOutPopup() {
        LocalDate today = LocalDate.now(JAKARTA_ZONE);

        // Must be a working day (i.e., user has an active schedule for today)
        if (!isWorkingDay(today)) {
            return false;
        }

        // Get current user's person
        HrPerson employee = currentUser.getPerson(); // adjust based on your model

        // If on approved leave today → skip check-in
        if (isOnApprovedLeave(today, employee)) {
            return false;
        }

        // Fetch today's attendance record
        Optional<HrAttendance> todayAttendance = attendanceRepo.findByAppUserIdAndAttendanceDate(
                currentUser.getId(), today
        );

        // Must have checked in
        if (todayAttendance.isEmpty() || todayAttendance.get().getCheckIn() == null) {
            return false;
        }

        HrAttendance attendance = todayAttendance.get();

        // Already checked out → no popup
        if (attendance.getCheckOut() != null) {
            return false;
        }

        // Get the work schedule linked to this attendance
        HrWorkSchedule schedule = attendance.getWorkSchedule();
        if (schedule == null) {
            return false; // Safety check
        }

        LocalTime scheduledCheckOut = schedule.getCheckOut(); // e.g., 17:00
        LocalTime now = LocalTime.now();

        // Optional: Add a 15-minute grace period before scheduled checkout
        // So popup appears starting 15 mins before official end time
        LocalTime earliestPopupTime = scheduledCheckOut.minusMinutes(15);

        // Show popup if current time >= (scheduled checkout - grace)
        return !now.isBefore(earliestPopupTime);
    }

    private boolean isWorkingDay(LocalDate date) {

        // Libur nasional tetap libur (untuk semua)
        if (calendarService.isHoliday(date)) {
            return false;
        }

        // Ambil schedule user pada tanggal tsb
        HrWorkSchedule schedule =
                workScheduleService.getActiveScheduleForUser(currentUser, date);

        if (schedule == null) {
            return false;
        }

        if (schedule.getType() == WorkScheduleType.Normal) {
            return date.getDayOfWeek().getValue() <= 5;
        }

        if (schedule.getType() == WorkScheduleType.Shift) {
            return true;
        }

        return false;
    }


    private boolean isOnApprovedLeave(LocalDate date, HrPerson employee) {
        List<LeaveStatusEnum> approvedStatuses = List.of(LeaveStatusEnum.APPROVED);
        // You might also include "PENDING" if you want to block check-in during pending leave
        // But typically, only approved leaves count.

        return hrLeaveApplicationRepository.existsByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
                employee, date, date, approvedStatuses
        );
    }

    public HrAttendance saveAttendance(HrAttendance att, AppUserInfo modifier) {
        // 1) hitung total work minutes dulu (berdasarkan check_in/out)
        fillTotalWorkMinutes(att);

        // 2) status existing
        setStatusBasedOnSchedule(att);

        return attendanceRepo.save(att);
    }

    public void deleteAttendance(HrAttendance attendance) {
        attendanceRepo.delete(attendance);
    }


    // ✅ Fixed: Accepts start/end and filters by attendanceDate
    public Page<HrAttendance> getAttendancePage(Pageable pageable, LocalDate start, LocalDate end, String searchTerm, HrCompany company, HrOrgStructure orgStructure, HrPerson emp) {
        if (currentUser == null) {
            throw new IllegalStateException("App user is not set. Please call setUser() before using this method.");
        }

        Specification<HrAttendance> spec = buildFilterSpec(start, end, searchTerm, company, orgStructure, emp);
        return attendanceRepo.findAll(spec, pageable);
    }

    public long countAttendance(LocalDate start, LocalDate end, String searchTerm, HrCompany company, HrOrgStructure orgStructure, HrPerson emp) {
        Specification<HrAttendance> spec = buildFilterSpec(start, end, searchTerm, company, orgStructure, emp);
        return attendanceRepo.count(spec);
    }

    public List<HrAttendance> getAttendanceList(LocalDate start,
                                                LocalDate end,
                                                String searchTerm,
                                                HrCompany company,
                                                HrOrgStructure orgStructure,
                                                HrPerson emp) {
        if (currentUser == null) {
            throw new IllegalStateException("App user is not set. Please call setUser() before using this method.");
        }

        Specification<HrAttendance> spec = buildFilterSpec(start, end, searchTerm, company, orgStructure, emp);

        Page<HrAttendance> page = attendanceRepo.findAll(
                spec,
                PageRequest.of(
                        0,
                        Integer.MAX_VALUE,
                        Sort.by(Sort.Direction.DESC, "attendanceDate")
                )
        );

        return page.getContent();
    }

    // ✅ Fixed: Now uses start/end and adds company/department filters
    private Specification<HrAttendance> buildFilterSpec(LocalDate start, LocalDate end, String searchTerm, HrCompany company, HrOrgStructure orgStructure, HrPerson emp) {
        Specification<HrAttendance> spec = buildBaseSearchSpec(searchTerm);

        // Filter by date range
        if (start != null || end != null) {
            spec = spec.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (start != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), start));
                }
                if (end != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), end));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            });
        }

        // Filter by company (via person -> employee -> department -> company)
        if (company != null) {
            spec = spec.and((root, query, cb) -> {
                Join<HrAttendance, HrPerson> personJoin = root.join("person");
                Join<HrPerson, HrPersonPosition> personPositionJoin = personJoin.join("personPosition");
                return cb.equal(personPositionJoin.get("company"), company);
            });
        }

        // Filter by department
        if (orgStructure != null) {
            spec = spec.and((root, query, cb) -> {
                Join<HrAttendance, HrPerson> personJoin = root.join("person");
                Join<HrPerson, HrPersonPosition> personPositionJoin = personJoin.join("personPosition");
                Join<HrPersonPosition, HrPosition> positionJoin = personPositionJoin.join("position");
                return cb.equal(positionJoin.get("orgStructure"), orgStructure);
            });
        }

        if(emp != null) {
            spec = spec.and((root, query, cb) -> {
                Join<HrAttendance, HrPerson> personJoin = root.join("person");
                return cb.equal(root.get("person"), emp);
            });
        }

        return spec;
    }

    private Specification<HrAttendance> buildBaseSearchSpec(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String lowerCaseSearchTerm = "%" + searchTerm.toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<HrAttendance, HrPerson> personJoin = root.join("person");
            return cb.or(
                    cb.like(cb.lower(personJoin.get("firstName")), lowerCaseSearchTerm),
                    cb.like(cb.lower(personJoin.get("lastName")), lowerCaseSearchTerm)
            );
        };
    }

    private void setStatusBasedOnSchedule(HrAttendance att) {

        if (att.getCheckIn() == null) {
            att.setStatus("ALPHA");
            return;
        }

        HrWorkSchedule schedule = att.getWorkSchedule();
        boolean overtimeAuto = Boolean.TRUE.equals(schedule.getIsOvertimeAuto());

        // ===============================
        // 1️⃣ BELUM CHECKOUT
        // ===============================
        if (att.getCheckOut() == null) {

            LocalDateTime scheduledOut = LocalDateTime.of(
                    att.getAttendanceDate(),
                    schedule.getCheckOut()
            );

            LocalDateTime batasLupa = scheduledOut.plusHours(1);
            LocalDateTime now = LocalDateTime.now(JAKARTA_ZONE);

            if (now.isAfter(batasLupa)) {
                att.setStatus("LUPA_CLOCK_OUT");
            }
            return;
        }

        // ===============================
        // 2️⃣ CHECKOUT SUDAH LEWAT HARI
        // ===============================
        if (att.getCheckOut().toLocalDate().isAfter(att.getAttendanceDate())) {
            att.setStatus("LUPA_CLOCK_OUT");
            return;
        }

        // ===============================
        // 3️⃣ NORMAL CHECKOUT (HARI YANG SAMA)
        // ===============================
        ZonedDateTime actualIn = att.getCheckIn().atZone(JAKARTA_ZONE);
        ZonedDateTime actualOut = att.getCheckOut().atZone(JAKARTA_ZONE);

        LocalDateTime scheduledIn = LocalDateTime.of(
                att.getAttendanceDate(),
                schedule.getCheckIn()
        );

        LocalDateTime scheduledOut = LocalDateTime.of(
                att.getAttendanceDate(),
                schedule.getCheckOut()
        );

        ZonedDateTime scheduledInZ = scheduledIn.atZone(JAKARTA_ZONE);
        ZonedDateTime scheduledOutZ = scheduledOut.atZone(JAKARTA_ZONE);

        String status = "HADIR";

        if (actualIn.isAfter(scheduledInZ.plusMinutes(15))) {
            status = "TERLAMBAT";
        }

        if (actualOut.isBefore(scheduledOutZ.minusMinutes(30))) {
            status = "PULANG_CEPAT";
        }
        else if (overtimeAuto && actualOut.isAfter(scheduledOutZ.plusHours(1))) {
            status = "OVERTIME";
        }

        att.setStatus(status);
    }

    public List<HrCompanyBranch> getBranchesForCurrentUserCompany() {
        if (currentUser == null) {
            throw new IllegalStateException("App user is not set. Please call setUser() before using this method.");
        }
        if (currentUser.getCompany() == null) {
            return List.of();
        }
        return hrCompanyBranchRepository.findByCompanyOrderByBranchNameAsc(currentUser.getCompany());
    }

    private void fillTotalWorkMinutes(HrAttendance att) {
        if (att == null) return;

        // default null / 0 sesuai kebutuhan UI kamu
        if (att.getCheckIn() == null || att.getCheckOut() == null) {
            att.setTotalWorkMinutes(null); // atau 0
            return;
        }

        // Validasi: clock-out harus >= clock-in
        if (att.getCheckOut().isBefore(att.getCheckIn())) {
            att.setTotalWorkMinutes(null); // atau 0
            return;
        }

        long minutes = java.time.Duration.between(att.getCheckIn(), att.getCheckOut()).toMinutes();

        // kalau mau batas maksimum (mis. 24 jam) silakan, tapi optional
        if (minutes < 0) minutes = 0;

        att.setTotalWorkMinutes((int) minutes);
    }

    // ------------------------------------------------------------------------
    // Methods merged from the former MobileAttendanceService.
    // Adapters (REST controllers) call these directly and shape the response;
    // these methods accept resolved entities and return the saved {@link HrAttendance},
    // throwing domain exceptions on rule violations.
    // ------------------------------------------------------------------------

    /**
     * Mobile-style check-in. Validates user has person + company assigned, ensures no existing check-in for the day,
     * resolves the active work schedule, evaluates geofence against company branches, and persists the attendance row.
     *
     * <p>The saved {@link HrAttendance#getStatus()} reflects whether the check-in was inside or outside the geofence
     * ({@code HADIR}, {@code TERLAMBAT}, {@code DI_LUAR_LOKASI}, {@code TERLAMBAT_DI_LUAR_LOKASI}). Adapters can use that
     * field to shape user-facing messages without an extra service round-trip.</p>
     *
     * @throws EntityNotFoundException        if the user has no person or company association
     * @throws WorkScheduleNotFoundException  if no active schedule exists for the user on that date
     * @throws AlreadyCheckedInException      if a check-in already exists for the same day
     */
    public HrAttendance checkIn(FwAppUser user, LocalDateTime submittedAt, Double lat, Double lon) {
        if (user.getPerson() == null || user.getPerson().getId() == null) {
            throw new EntityNotFoundException("HrPerson", "appUser=" + user.getId());
        }
        if (user.getCompany() == null || user.getCompany().getId() == null) {
            throw new EntityNotFoundException("HrCompany", "appUser=" + user.getId());
        }

        LocalDate attendanceDate = submittedAt.atZone(JAKARTA_ZONE).toLocalDate();

        Optional<HrAttendance> existing = attendanceRepo.findByAppUserIdAndAttendanceDate(user.getId(), attendanceDate);
        if (existing.isPresent() && existing.get().getCheckIn() != null) {
            throw new AlreadyCheckedInException(user.getPerson().getId(), attendanceDate);
        }

        HrWorkSchedule schedule = workScheduleService.getActiveScheduleForUser(user, attendanceDate);
        if (schedule == null || schedule.getId() == null) {
            throw new WorkScheduleNotFoundException(user.getId(), attendanceDate);
        }

        HrCompanyBranch matchedBranch = resolveBranch(user.getCompany().getId(), lat, lon);
        boolean isOutside = (matchedBranch == null);

        HrAttendance toSave = existing.orElseGet(HrAttendance::new);
        toSave.setAppUser(user);
        toSave.setPerson(user.getPerson());
        toSave.setWorkSchedule(schedule);
        toSave.setAttendanceDate(attendanceDate);
        toSave.setCheckIn(submittedAt);
        toSave.setCreatedAt(submittedAt);
        toSave.setUpdatedAt(submittedAt);

        if (!isOutside) {
            toSave.setBranchCode(matchedBranch.getBranchCode());
            toSave.setBranchName(matchedBranch.getBranchName());
            toSave.setBranchAddress(matchedBranch.getBranchAddress());
        } else {
            toSave.setBranchCode("OUTSIDE");
            toSave.setBranchName("OUTSIDE_LOCATION");
            toSave.setBranchAddress(null);
        }

        toSave.setViaDevice("MOBILE");
        toSave.setCheckInLat(lat);
        toSave.setCheckInLon(lon);

        String baseStatus = statusForCheckInOnly(submittedAt.toLocalTime(), schedule.getCheckIn());
        if (!isOutside) {
            toSave.setStatus(baseStatus); // HADIR / TERLAMBAT
        } else {
            toSave.setStatus("TERLAMBAT".equals(baseStatus) ? "TERLAMBAT_DI_LUAR_LOKASI" : "DI_LUAR_LOKASI");
            toSave.setNotes("CHECKIN_OUTSIDE_GEOFENCE");
        }

        toSave.setTotalWorkMinutes(null);
        return attendanceRepo.save(toSave);
    }

    /**
     * Mobile-style check-out. Locates today's open check-in row for the user, computes worked minutes,
     * resolves a final status (PULANG_CEPAT / OVERTIME / unchanged), and persists.
     *
     * @throws NotCheckedInException if no open check-in exists for the day
     * @throws IllegalArgumentException if the resolved duration is non-positive (clock skew or future submittedAt)
     */
    public HrAttendance checkOut(FwAppUser user, LocalDateTime submittedAt, Double lat, Double lon) {
        LocalDate today = submittedAt.atZone(JAKARTA_ZONE).toLocalDate();

        HrAttendance attendance = attendanceRepo
                .findByAppUserIdAndAttendanceDateAndCheckOutIsNull(user.getId(), today)
                .orElseThrow(() -> new NotCheckedInException(
                        user.getPerson() != null ? user.getPerson().getId() : null, today));

        if (attendance.getCheckIn() == null) {
            throw new NotCheckedInException(
                    user.getPerson() != null ? user.getPerson().getId() : null, today);
        }

        long totalMinutes = Duration.between(attendance.getCheckIn(), submittedAt).toMinutes();
        if (totalMinutes <= 0) {
            throw new IllegalArgumentException("Check-out time must be after check-in (computed " + totalMinutes + " minutes).");
        }

        HrWorkSchedule schedule = workScheduleService.getActiveScheduleForUser(user, today);
        String finalStatus = resolveCheckOutStatus(attendance.getStatus(), submittedAt.toLocalTime(), schedule);

        attendance.setCheckOut(submittedAt);
        attendance.setCheckOutLat(lat);
        attendance.setCheckOutLon(lon);
        attendance.setTotalWorkMinutes((int) totalMinutes);
        attendance.setStatus(finalStatus);
        attendance.setUpdatedAt(LocalDateTime.now());
        return attendanceRepo.save(attendance);
    }

    /**
     * Resolve which company branch the given GPS coordinates fall inside, by matching against the office_location
     * geofence polygons. Returns {@code null} if no branch matches (caller treats as "outside geofence").
     */
    private HrCompanyBranch resolveBranch(Long companyId, Double lat, Double lon) {
        List<HrCompanyBranch> branches = hrCompanyBranchRepository.findByCompany_Id(companyId);
        if (branches == null || branches.isEmpty()) return null;

        Long[] branchIds = branches.stream().map(HrCompanyBranch::getId).toArray(Long[]::new);
        HrOfficeLocation matchedOffice = officeLocationRepo.findMatchedOffice(lat, lon, branchIds);
        if (matchedOffice == null || matchedOffice.getBranchOfficeId() == null) return null;

        Long matchedBranchId = matchedOffice.getBranchOfficeId();
        return branches.stream().filter(b -> matchedBranchId.equals(b.getId())).findFirst().orElse(null);
    }

    private String statusForCheckInOnly(LocalTime actualIn, LocalTime scheduledIn) {
        if (actualIn == null || scheduledIn == null) return "HADIR";
        return actualIn.isAfter(scheduledIn.plusMinutes(15)) ? "TERLAMBAT" : "HADIR";
    }

    private String resolveCheckOutStatus(String currentStatus, LocalTime actualOut, HrWorkSchedule schedule) {
        LocalTime scheduledOut = schedule.getCheckOut();

        if (actualOut.isBefore(scheduledOut.minusMinutes(15))) {
            return "PULANG_CEPAT";
        }
        if (actualOut.isAfter(scheduledOut.plusMinutes(15))
                && Boolean.TRUE.equals(schedule.getIsOvertimeAuto())) {
            return "OVERTIME";
        }
        return currentStatus;
    }
}