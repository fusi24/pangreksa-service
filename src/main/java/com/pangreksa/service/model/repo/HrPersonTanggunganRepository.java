package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrPerson;
import com.pangreksa.service.model.entity.HrPersonTanggungan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrPersonTanggunganRepository
        extends JpaRepository<HrPersonTanggungan, Long> {

    List<HrPersonTanggungan> findByPerson(HrPerson person);
}
