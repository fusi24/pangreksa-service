package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrDepartment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface HrDepartmentRepository extends CrudRepository<HrDepartment, Long>, JpaSpecificationExecutor<HrDepartment> {
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByNameAndIdNot(String name, Long id);
}
