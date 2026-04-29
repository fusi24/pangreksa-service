package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.entity.HrPersonContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPersonContactRepository extends JpaRepository<HrPersonContact, Long> {
    // Find by referenceId
    List<HrPersonContact> findByPerson(HrPerson person);
}
