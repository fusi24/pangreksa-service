package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrAttendanceViolation;
import com.pangreksa.service.model.entity.HrPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface HrAttendanceViolationRepository
        extends JpaRepository<HrAttendanceViolation, Long> {

    boolean existsByEmployeeAndAttendanceDateAndViolationType(
            HrPerson employee,
            LocalDate attendanceDate,
            String violationType
    );
}
