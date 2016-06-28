package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Model;

public interface ModelRepository extends PagingAndSortingRepository<Model, Long> {

}
