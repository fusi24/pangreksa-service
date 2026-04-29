package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrCompany;
import com.pangreksa.service.model.entity.HrLeaveGenerationLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrLeaveGenerationLogRepository extends JpaRepository<HrLeaveGenerationLog, Long> {
    // Find by referenceId
    //List<HrLeaveGenerationLog> findByReferenceId(Long referenceId);
    @EntityGraph(attributePaths = {"createdBy","updatedBy","company"})
    List<HrLeaveGenerationLog> findByCompanyOrderByYearDesc(HrCompany company);
}
