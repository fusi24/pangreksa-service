package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrLeaveRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrLeaveRequestLogRepository extends JpaRepository<HrLeaveRequestLog, Long> {
    // Find by referenceId
    //List<HrLeaveRequestLog> findByReferenceId(Long referenceId);
}
