package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Craft;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;

public interface CraftRepository extends JpaRepository<Craft, Long> {

	Page<Phase> findByModel(@Param("model") Model model, Pageable pageable);
	
}
