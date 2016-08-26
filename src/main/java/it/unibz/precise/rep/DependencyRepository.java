package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;

public interface DependencyRepository extends PagingAndSortingRepository<Dependency, Long> {
	
	Page<Dependency> findByModel(@Param("model") Model model, Pageable pageable);

}
