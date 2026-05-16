package com.pangreksa.service.service;

import com.pangreksa.service.model.entity.*;
import com.pangreksa.service.model.enumerate.AttendanceCorrectionStatusEnum;
import com.pangreksa.service.model.exception.AttendanceCorrectionStateException;
import com.pangreksa.service.model.exception.AttendanceCorrectionValidationException;
import com.pangreksa.service.model.exception.WorkScheduleNotFoundException;
import com.pangreksa.service.model.repo.FwAppUserRepository;
import com.pangreksa.service.model.repo.HrAttendanceCorrectionRepository;
import com.pangreksa.service.model.repo.HrAttendanceRepository;
import com.pangreksa.service.model.repo.HrNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceCorrectionService {

    private final HrAttendanceCorrectionRepository correctionRepository;
    private final HrAttendanceRepository attendanceRepository;
    private final HrNotificationRepository notificationRepository;
    private final FwAppUserRepository appUserRepository;
    private final AttendanceService attendanceService;
    private final HrWorkScheduleService workScheduleService;

    public AttendanceCorrectionService(HrAttendanceCorrectionRepository correctionRepository,
                                       HrAttendanceRepository attendanceRepository,
                                       HrNotificationRepository notificationRepository,
                                       FwAppUserRepository appUserRepository,
                                       AttendanceService attendanceService,
                                       HrWorkScheduleService workScheduleService) {
        this.correctionRepository = correctionRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationRepository = notificationRepository;
        this.appUserRepository = appUserRepository;
        this.attendanceService = attendanceService;
        this.workScheduleService = workScheduleService;
    }

    @Transactional
    public HrAttendanceCorrection submitCorrection(HrAttendanceCorrection draft, FwAppUser submitter) {
        validateSubmission(draft);

        LocalDate targetDate = resolveTargetDate(draft);
        Optional<HrAttendance> existing = attendanceRepository.findByAppUserIdAndAttendanceDate(submitter.getId(), targetDate);
        HrAttendance attendance = existing.orElseGet(HrAttendance::new);

        // Snapshots keep auditability of what was changed and why.
        draft.setAttendance(attendance);
        draft.setEmployee(submitter.getPerson());
        draft.setCompany(submitter.getCompany());
        draft.setStatus(AttendanceCorrectionStatusEnum.SUBMITTED);
        draft.setSubmittedAt(LocalDateTime.now());
        draft.setOriginalAttendanceDate(attendance.getAttendanceDate());
        draft.setOriginalCheckIn(attendance.getCheckIn());
        draft.setOriginalCheckOut(attendance.getCheckOut());
        draft.setCreatedBy(submitter);
        draft.setUpdatedBy(submitter);
        draft.setUpdatedAt(LocalDateTime.now());

        HrAttendanceCorrection saved = correctionRepository.save(draft);
        notifyApproverOnSubmit(saved);
        return saved;
    }

    @Transactional
    public HrAttendanceCorrection approveCorrection(HrAttendanceCorrection correction, FwAppUser approver) {
        ensurePending(correction);

        HrAttendance attendance = resolveAttendanceForApproval(correction);
        applyRequestedChange(attendance, correction);

        attendanceService.saveAttendance(attendance, null);

        correction.setStatus(AttendanceCorrectionStatusEnum.APPROVED);
        correction.setApprovedBy(approver.getPerson());
        correction.setApprovedAt(LocalDateTime.now());
        correction.setUpdatedBy(approver);
        correction.setUpdatedAt(LocalDateTime.now());

        HrAttendanceCorrection saved = correctionRepository.save(correction);
        notifyEmployeeOnDecision(saved, "Pengajuan koreksi absensi disetujui");
        return saved;
    }

    @Transactional
    public HrAttendanceCorrection rejectCorrection(HrAttendanceCorrection correction, FwAppUser approver, String rejectionReason) {
        ensurePending(correction);

        correction.setStatus(AttendanceCorrectionStatusEnum.REJECTED);
        correction.setRejectionReason(rejectionReason);
        correction.setApprovedBy(approver.getPerson());
        correction.setApprovedAt(LocalDateTime.now());
        correction.setUpdatedBy(approver);
        correction.setUpdatedAt(LocalDateTime.now());

        HrAttendanceCorrection saved = correctionRepository.save(correction);
        notifyEmployeeOnDecision(saved, "Pengajuan koreksi absensi ditolak");
        return saved;
    }

    public List<HrAttendanceCorrection> getSubmissionHistory(HrPerson employee, int monthsBack) {
        LocalDateTime now = LocalDateTime.now();
        return correctionRepository.findByEmployeeAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                employee,
                now.minusMonths(monthsBack),
                now
        );
    }

    public List<HrAttendanceCorrection> getPendingApprovals(HrPerson approver, int monthsBack) {
        LocalDateTime now = LocalDateTime.now();
        return correctionRepository.findBySubmittedToAndSubmittedAtBetweenAndStatusInOrderBySubmittedAtDesc(
                approver,
                now.minusMonths(monthsBack),
                now,
                List.of(AttendanceCorrectionStatusEnum.SUBMITTED)
        );
    }

    private void validateSubmission(HrAttendanceCorrection draft) {
        if (draft == null) {
            throw new AttendanceCorrectionValidationException("Draft correction is required");
        }
        if (draft.getRequestedCheckIn() == null && draft.getRequestedCheckOut() == null) {
            throw new AttendanceCorrectionValidationException("At least one of requested check-in/check-out must be filled");
        }
        LocalDateTime requestedIn = draft.getRequestedCheckIn();
        LocalDateTime requestedOut = draft.getRequestedCheckOut();
        if (requestedIn != null && requestedOut != null && requestedOut.isBefore(requestedIn)) {
            throw new AttendanceCorrectionValidationException("Requested check-out must be after check-in");
        }
        if (draft.getSubmittedTo() == null) {
            throw new AttendanceCorrectionValidationException("Approver (submittedTo) is required");
        }
    }

    private void ensurePending(HrAttendanceCorrection correction) {
        if (correction.getStatus() != AttendanceCorrectionStatusEnum.SUBMITTED) {
            throw new AttendanceCorrectionStateException("Only SUBMITTED correction can be processed");
        }
    }

    private LocalDate resolveTargetDate(HrAttendanceCorrection draft) {
        if (draft.getRequestedAttendanceDate() != null) {
            return draft.getRequestedAttendanceDate();
        }
        if (draft.getRequestedCheckIn() != null) {
            return draft.getRequestedCheckIn().toLocalDate();
        }
        return draft.getRequestedCheckOut().toLocalDate();
    }

    private HrAttendance resolveAttendanceForApproval(HrAttendanceCorrection correction) {
        HrAttendance attendance = correction.getAttendance();
        if (attendance != null && attendance.getId() != null) {
            return attendance;
        }

        LocalDate targetDate = resolveTargetDate(correction);
        return attendanceRepository.findByAppUserIdAndAttendanceDate(
                correction.getCreatedBy().getId(), targetDate
        ).orElseGet(HrAttendance::new);
    }

    private void applyRequestedChange(HrAttendance attendance, HrAttendanceCorrection correction) {
        LocalDate targetDate = resolveTargetDate(correction);

        if (attendance.getId() == null) {
            attendance.setAppUser(correction.getCreatedBy());
            attendance.setPerson(correction.getEmployee());
            HrWorkSchedule schedule = workScheduleService.getActiveScheduleForUser(correction.getCreatedBy(), targetDate);
            if (schedule == null) {
                throw new WorkScheduleNotFoundException(correction.getCreatedBy().getId(), targetDate);
            }
            attendance.setWorkSchedule(schedule);
        }

        attendance.setAttendanceDate(targetDate);
        if (correction.getRequestedCheckIn() != null) {
            attendance.setCheckIn(correction.getRequestedCheckIn());
        }
        if (correction.getRequestedCheckOut() != null) {
            attendance.setCheckOut(correction.getRequestedCheckOut());
        }
        attendance.setNotes(correction.getReason());
    }

    private void notifyApproverOnSubmit(HrAttendanceCorrection correction) {
        if (correction.getSubmittedTo() == null) return;

        appUserRepository.findByPerson(correction.getSubmittedTo()).ifPresent(approver ->
                createNotification(
                        approver.getUsername(),
                        "Pengajuan koreksi absensi menunggu approval",
                        "ATTENDANCE_CORRECTION",
                        correction.getId()
                )
        );
    }

    private void notifyEmployeeOnDecision(HrAttendanceCorrection correction, String title) {
        appUserRepository.findByPerson(correction.getEmployee()).ifPresent(employee ->
                createNotification(
                        employee.getUsername(),
                        title,
                        "ATTENDANCE_CORRECTION",
                        correction.getId()
                )
        );
    }

    private void createNotification(String username, String title, String type, Long referenceId) {
        HrNotification notif = new HrNotification();
        notif.setUsername(username);
        notif.setTitle(title);
        notif.setType(type);
        notif.setReferenceId(referenceId);
        notif.setIsRead(false);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);
    }
}
