package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrTaxBracket;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HrTaxBracketRepository extends CrudRepository<HrTaxBracket, Long> {

    public List<HrTaxBracket> findAllByOrderByMinIncomeAsc();
}
