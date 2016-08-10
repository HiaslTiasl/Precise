package it.unibz.precise.rep;

import org.springframework.data.jpa.repository.JpaRepository;

import it.unibz.precise.model.Model;

public interface ModelRepository extends JpaRepository<Model, Long> {

	Model findByName(String name);
}
