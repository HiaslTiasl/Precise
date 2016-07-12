package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import it.unibz.precise.model.ConstructionUnit;

@RepositoryRestResource(exported=true)
public interface ConstructionUnitRepository extends PagingAndSortingRepository<ConstructionUnit, Long> {

}
