package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Constraint;

public interface FlowConstraintRepository extends PagingAndSortingRepository<Constraint, Long> {

}
