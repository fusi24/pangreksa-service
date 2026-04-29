package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrCompany;
import com.pangreksa.service.model.entity.HrCompanyBranch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrCompanyBranchRepository extends JpaRepository<HrCompanyBranch, Long> {

    @EntityGraph(attributePaths = {"company"})
    List<HrCompanyBranch> findAll();

    @EntityGraph(attributePaths = {"company"})
    List<HrCompanyBranch> findByCompanyId(Long companyId);

    boolean existsByCompanyIdAndBranchCodeIgnoreCase(Long companyId, String branchCode);

    List<HrCompanyBranch> findByCompanyOrderByBranchNameAsc(HrCompany company);

    // BE-merged
    List<HrCompanyBranch> findByCompany_Id(Long companyId);

    java.util.Optional<HrCompanyBranch> findByCompany_IdAndBranchCode(Long companyId, String branchCode);

}
