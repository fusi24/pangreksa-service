package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrContract;
import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.enumerate.ContractStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrContractRepository extends JpaRepository<HrContract, Long> {

    List<HrContract> findByStatus(ContractStatusEnum status);

    List<HrContract> findByPersonAndStatus(
            HrPerson person,
            ContractStatusEnum status
    );
}