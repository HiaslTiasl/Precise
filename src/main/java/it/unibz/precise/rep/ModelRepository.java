package it.unibz.precise.rep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;

/**
 * Repository for {@link Model}s.
 * 
 * @author MatthiasP
 *
 */
public interface ModelRepository extends JpaRepository<Model, Long> {

	/** Find a single {@code Model} by name (which is unique). */
	Model findByName(@Param("name") String name);
	
}
