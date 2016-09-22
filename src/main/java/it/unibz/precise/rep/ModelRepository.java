package it.unibz.precise.rep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.projection.ModelSummaryProjection;

@RepositoryRestResource(excerptProjection=ModelSummaryProjection.class)
public interface ModelRepository extends JpaRepository<Model, Long> {

	Model findByName(@Param("name") String name);
	
}
