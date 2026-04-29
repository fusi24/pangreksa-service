package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.FwMenuGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FwMenuGroupRepository extends CrudRepository<FwMenuGroup, Long> {

    public List<FwMenuGroup> findByIsActiveTrue();
}
