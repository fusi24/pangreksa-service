package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPayrollComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrPayrollComponentRepository extends JpaRepository<HrPayrollComponent, Long> {

    List<HrPayrollComponent> findByPayrollCalculationId(Long payrollCalculationId);

    List<HrPayrollComponent> findByPayrollCalculationIdOrderBySortOrderAsc(Long payrollCalculationId);

    void deleteByPayrollCalculationId(Long payrollCalculationId);
}