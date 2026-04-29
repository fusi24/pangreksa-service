package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.entity.HrPersonAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPersonAddressRepository extends JpaRepository<HrPersonAddress, Long> {
    // Find by referenceId
    List<HrPersonAddress> findByPerson(HrPerson person);
}
