package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrCompany;
import com.pangreksa.service.model.entity.HrPosition;
import com.pangreksa.service.model.entity.HrSalaryPositionAllowance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrSalaryPositionAllowanceRepository extends JpaRepository<HrSalaryPositionAllowance, Long> {
    // Find All by Position ID and End Date is null order by updatedAt asc
    @EntityGraph(attributePaths = {"allowance","company"})
    List<HrSalaryPositionAllowance> findByPositionAndCompanyAndEndDateIsNullOrderByUpdatedAtAsc(HrPosition position, HrCompany company);
    @EntityGraph(attributePaths = {"allowance","company"})
    List<HrSalaryPositionAllowance> findByPositionAndCompanyOrderByUpdatedAtAsc(HrPosition position, HrCompany company);
}
