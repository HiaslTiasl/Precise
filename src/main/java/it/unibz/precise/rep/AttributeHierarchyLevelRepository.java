package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.AttributeHierarchyLevel;

/**
 * Repository for {@link AttributeHierarchyLevel}s.
 * 
 * TODO: remove?
 * 
 * @author MatthiasP
 *
 */
public interface AttributeHierarchyLevelRepository extends PagingAndSortingRepository<AttributeHierarchyLevel, Long> {

}
