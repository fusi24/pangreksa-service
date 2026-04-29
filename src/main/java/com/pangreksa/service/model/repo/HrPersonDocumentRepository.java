package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.entity.HrPersonDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPersonDocumentRepository extends JpaRepository<HrPersonDocument, Long> {
    // Find by referenceId
    List<HrPersonDocument> findByPerson(HrPerson person);
}
