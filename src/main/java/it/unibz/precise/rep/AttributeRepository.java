package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Attribute;

public interface AttributeRepository extends PagingAndSortingRepository<Attribute, Long> {

}
