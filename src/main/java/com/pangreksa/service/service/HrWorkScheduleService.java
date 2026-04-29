package com.pangreksa.service.service;

import com.pangreksa.service.model.entity.FwAppUser;
import com.pangreksa.service.model.entity.HrPersonPosition;
import com.pangreksa.service.model.entity.HrWorkSchedule;
import com.pangreksa.service.model.entity.HrWorkScheduleAssignment;
import com.pangreksa.service.model.enumerate.WorkScheduleType;
import com.pangreksa.service.model.repo.HrPersonPositionRepository;
import com.pangreksa.service.model.repo.HrWorkScheduleAssignmentRepository;
import com.pangreksa.service.model.repo.HrWorkScheduleRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class HrWorkScheduleService {

    private static final Logger log =
            LoggerFactory.getLogger(HrWorkScheduleService.class);

    private final HrPersonPositionRepository personPositionRepo;
    private final HrWorkScheduleAssignmentRepository scheduleAssignmentRepo;
    private final HrWorkScheduleRepository workScheduleRepo;
    private final CalendarService calendarService;

    @Autowired
    public HrWorkScheduleService(
            HrPersonPositionRepository personPositionRepo,
            HrWorkScheduleAssignmentRepository scheduleAssignmentRepo,
            HrWorkScheduleRepository workScheduleRepo,
            CalendarService calendarService) {

        this.personPositionRepo = personPositionRepo;
        this.scheduleAssignmentRepo = scheduleAssignmentRepo;
        this.workScheduleRepo = workScheduleRepo;
        this.calendarService = calendarService;
    }

    /**
     * Mengambil jadwal kerja aktif untuk user pada tanggal tertentu.
     * Prioritas:
     * 1. Schedule assignment khusus (Selected) berdasarkan org structure
     * 2. Fallback ke schedule assignment_type = All
     * Syarat:
     * - is_active = true
     * - effective_date <= attendanceDate
     */
    public HrWorkSchedule getActiveScheduleForUser(FwAppUser user, LocalDate attendanceDate) {

        if (user == null || user.getPerson() == null || attendanceDate == null) {
            log.warn("Schedule lookup failed: user/person/date is null");
            return null;
        }

        Long personId = user.getPerson().getId();
        log.info("Looking up schedule for personId={}, date={}", personId, attendanceDate);

        // 1. Ambil posisi aktif karyawan
        HrPersonPosition position =
                personPositionRepo.findCurrentPositionsByCompanyAndPerson(
                        user.getCompany(),
                        user.getPerson(),
                        attendanceDate
                );

        if (position == null || position.getPosition() == null) {
            log.warn("No active position found for personId={} on date={}", personId, attendanceDate);
            return null;
        }

        Long orgStructureId = position.getPosition().getOrgStructure().getId();
        log.info("OrgStructureId={}", orgStructureId);

        Optional<HrWorkScheduleAssignment> assignmentOpt =
                scheduleAssignmentRepo
                        .findFirstByOrgStructureIdAndSchedule_EffectiveDateLessThanEqualAndSchedule_IsActiveTrue(
                                orgStructureId,
                                attendanceDate
                        );

        if (assignmentOpt.isPresent()) {
            HrWorkSchedule schedule = assignmentOpt.get().getSchedule();

            log.info("Selected schedule found: id={}, effectiveDate={}, active={}",
                    schedule.getId(),
                    schedule.getEffectiveDate(),
                    schedule.getIsActive());

            if (Boolean.TRUE.equals(schedule.getIsActive())
                    && !schedule.getEffectiveDate().isAfter(attendanceDate)) {

                log.info("Selected schedule is VALID");
                return schedule;
            }

            log.warn("Selected schedule is NOT valid for date {}", attendanceDate);
        } else {
            log.info("No selected schedule for orgStructureId={}", orgStructureId);
        }

        // 3. Fallback ke schedule All
        Optional<HrWorkSchedule> allScheduleOpt =
                workScheduleRepo.findFirstByAssignmentScope("All");

        if (allScheduleOpt.isPresent()) {
            HrWorkSchedule allSchedule = allScheduleOpt.get();

            log.info("All schedule found: id={}, effectiveDate={}, active={}",
                    allSchedule.getId(),
                    allSchedule.getEffectiveDate(),
                    allSchedule.getIsActive());

            if (Boolean.TRUE.equals(allSchedule.getIsActive())
                    && !allSchedule.getEffectiveDate().isAfter(attendanceDate)) {

                log.info("All schedule is VALID");
                return allSchedule;
            }

            log.warn("All schedule is NOT valid for date {}", attendanceDate);
        } else {
            log.warn("No schedule with assignment_type=All found");
        }

        log.error("No valid work schedule resolved for personId={} on date={}",
                personId, attendanceDate);

        return null;
    }

    /**
     * Helper untuk cek apakah user punya jadwal kerja aktif di tanggal tertentu
     */
    public boolean hasActiveScheduleForUser(FwAppUser user, LocalDate date) {
        return getActiveScheduleForUser(user, date) != null;
    }

    /**
     * Whether the given user is expected to work on the given date — accounts for company calendar holidays
     * and the user's resolved schedule type (Normal = Mon–Fri, Shift = always working when scheduled).
     * Merged from the former MobileWorkScheduleService.
     */
    public boolean isWorkingDay(FwAppUser user, LocalDate date) {
        if (calendarService.isHoliday(date)) {
            return false;
        }
        HrWorkSchedule schedule = getActiveScheduleForUser(user, date);
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
}
