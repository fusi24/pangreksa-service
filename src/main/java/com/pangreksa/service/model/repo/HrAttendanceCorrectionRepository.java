package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrAttendanceCorrection;
import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.enumerate.AttendanceCorrectionStatusEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HrAttendanceCorrectionRepository
        extends JpaRepository<HrAttendanceCorrection, Long> {

    @EntityGraph(attributePaths = {
            "employee",
            "submittedTo",
            "approvedBy",
            "attendance"
    })
    List<HrAttendanceCorrection>
    findByEmployeeAndSubmittedAtBetweenOrderBySubmittedAtDesc(
            HrPerson employee,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    @EntityGraph(attributePaths = {
            "employee",
            "submittedTo",
            "approvedBy",
            "attendance"
    })
    List<HrAttendanceCorrection>
    findByEmployee_IdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
            Long employeeId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    @EntityGraph(attributePaths = {
            "employee",
            "submittedTo",
            "approvedBy",
            "attendance"
    })
    List<HrAttendanceCorrection>
    findByEmployee_IdOrderBySubmittedAtDesc(
            Long employeeId
    );

    @EntityGraph(attributePaths = {
            "employee",
            "submittedTo",
            "approvedBy",
            "attendance"
    })
    List<HrAttendanceCorrection>
    findBySubmittedToAndSubmittedAtBetweenAndStatusInOrderBySubmittedAtDesc(
            HrPerson submittedTo,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<AttendanceCorrectionStatusEnum> statuses
    );
}
