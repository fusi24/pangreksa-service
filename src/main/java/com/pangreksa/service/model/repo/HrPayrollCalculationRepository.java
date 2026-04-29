package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPayrollCalculation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HrPayrollCalculationRepository extends CrudRepository<HrPayrollCalculation, Long> {

    public HrPayrollCalculation findFirstByPayrollId(Long payrollId);

    void deleteByPayrollIdIn(List<Long> payrollIds);

}
