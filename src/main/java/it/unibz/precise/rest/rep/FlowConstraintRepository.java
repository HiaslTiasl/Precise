package it.unibz.precise.rest.rep;

import it.unibz.precise.model.Constraint;
import it.unibz.precise.model.ConstraintKind;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlowConstraintRepository extends PagingAndSortingRepository<Constraint<? extends ConstraintKind>, Long> {

}
