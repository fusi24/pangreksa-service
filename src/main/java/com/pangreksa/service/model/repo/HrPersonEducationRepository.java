package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.entity.HrPersonEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPersonEducationRepository extends JpaRepository<HrPersonEducation, Long> {
    // Find by referenceId
    List<HrPersonEducation> findByPerson(HrPerson person);
}
