package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrAttendancePenalty;
import com.pangreksa.service.model.entity.HrPerson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrAttendancePenaltyRepository
        extends JpaRepository<HrAttendancePenalty, Long> {

    boolean existsByEmployeeAndReference_Id(HrPerson employee, Long referenceId);
}
