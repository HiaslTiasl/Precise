package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Activity;

/**
 * Repository for {@link Activity}s.
 * 
 * @author MatthiasP
 *
 */
public interface ActivityRepository extends PagingAndSortingRepository<Activity, Long> {
	
	// TODO: remove?
	Page<Activity> findByModel(@Param("model") Model model, Pageable p);
	
	/** Find all activities in the given {@link Phase}. */
	Page<Activity> findByPhase(@Param("phase") Phase phase, Pageable p);

}
