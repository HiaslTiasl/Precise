package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;

/**
 * Repository for {@link Phase}s.
 * 
 * @author MatthiasP
 *
 */
public interface PhaseRepository extends PagingAndSortingRepository<Phase, Long> {
	
	// TODO: remove?
	Page<Phase> findByModel(@Param("model") Model model, Pageable pageable);
	
}
