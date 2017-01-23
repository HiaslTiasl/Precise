package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Craft;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;

/**
 * Repository for {@link Craft}s.
 * 
 * @author MatthiasP
 *
 */
public interface CraftRepository extends JpaRepository<Craft, Long> {

	// TODO: remove?
	Page<Phase> findByModel(@Param("model") Model model, Pageable pageable);
	
}
