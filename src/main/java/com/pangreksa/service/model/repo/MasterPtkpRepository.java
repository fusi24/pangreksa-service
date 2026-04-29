package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.MasterPtkp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterPtkpRepository extends JpaRepository<MasterPtkp, Long> {

    Optional<MasterPtkp> findByKodePtkpAndAktifTrue(String kodePtkp);

    Optional<MasterPtkp> findFirstByKodePtkpAndAktifTrue(String kodePtkp);
}