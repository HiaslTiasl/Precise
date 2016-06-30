package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Flow;

public interface FlowRepository extends PagingAndSortingRepository<Flow, Long> {

}
