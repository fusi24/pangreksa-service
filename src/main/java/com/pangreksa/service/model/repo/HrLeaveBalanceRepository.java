package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrCompany;
import com.pangreksa.service.model.entity.HrLeaveAbsenceTypes;
import com.pangreksa.service.model.entity.HrLeaveBalance;
import com.pangreksa.service.model.entity.HrPerson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrLeaveBalanceRepository extends JpaRepository<HrLeaveBalance, Long> {

    List<HrLeaveBalance> findAllByYearAndCompany(int year, HrCompany company);

    HrLeaveBalance findByEmployeeAndYearAndLeaveAbsenceTypeAndCompany(HrPerson person, int year, HrLeaveAbsenceTypes leaveAbsenceTypes, HrCompany company);
    @EntityGraph(attributePaths = {"leaveAbsenceType"})
    List<HrLeaveBalance> findByEmployeeAndYearAndCompany(HrPerson person, int year, HrCompany company);

    long countByCompanyAndYear(HrCompany company, int year);

    // BE-merged: includes leaveType discriminator (e.g. 'ANNUAL' vs 'CARRY')
    HrLeaveBalance findByEmployeeAndYearAndLeaveTypeAndLeaveAbsenceTypeAndCompany(
            HrPerson employee,
            int year,
            String leaveType,
            HrLeaveAbsenceTypes leaveAbsenceType,
            HrCompany company
    );
}
