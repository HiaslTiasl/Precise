package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Constraint;
import it.unibz.precise.model.ConstraintKind;

public interface FlowConstraintRepository extends PagingAndSortingRepository<Constraint<? extends ConstraintKind>, Long> {

}
