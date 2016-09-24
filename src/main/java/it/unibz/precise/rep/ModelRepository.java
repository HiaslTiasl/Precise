package it.unibz.precise.rep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;

public interface ModelRepository extends JpaRepository<Model, Long> {

	Model findByName(@Param("name") String name);
	
}
