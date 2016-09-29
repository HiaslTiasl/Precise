package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;

public interface PhaseRepository extends PagingAndSortingRepository<Phase, Long> {
	
	Page<Phase> findByModel(@Param("model") Model model, Pageable pageable);
	
}
