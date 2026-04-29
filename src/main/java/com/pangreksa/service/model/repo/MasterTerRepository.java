package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.MasterTer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterTerRepository extends JpaRepository<MasterTer, Long> {
    Optional<MasterTer> findFirstByMasterPtkpIdAndAktifTrue(Long masterPtkpId);
}