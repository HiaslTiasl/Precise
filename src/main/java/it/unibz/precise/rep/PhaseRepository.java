package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Phase;

public interface PhaseRepository extends PagingAndSortingRepository<Phase, Long> {
	
}
