package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Dependency;

public interface DependencyRepository extends PagingAndSortingRepository<Dependency, Long> {

}
