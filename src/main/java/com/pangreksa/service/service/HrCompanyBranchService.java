package com.pangreksa.service.service;

import com.pangreksa.service.model.entity.HrCompanyBranch;
import com.pangreksa.service.model.repo.HrCompanyBranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HrCompanyBranchService {

    private final HrCompanyBranchRepository repo;

    public HrCompanyBranchService(HrCompanyBranchRepository repo) {
        this.repo = repo;
    }

    public List<HrCompanyBranch> findAll() {
        return repo.findAll();
    }
}
