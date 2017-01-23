package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Attribute;

/**
 * Repository for {@link Attribute}s.
 * 
 * TODO: remove?
 * 
 * @author MatthiasP
 *
 */
public interface AttributeRepository extends PagingAndSortingRepository<Attribute, Long> {

}
