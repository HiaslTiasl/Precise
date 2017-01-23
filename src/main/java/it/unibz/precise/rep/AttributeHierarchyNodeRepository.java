package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.AttributeHierarchyNode;

/**
 * Repository for {@link AttributeHierarchyNode}s.
 * 
 * TODO: remove?
 * 
 * @author MatthiasP
 *
 */
public interface AttributeHierarchyNodeRepository extends PagingAndSortingRepository<AttributeHierarchyNode, Long> {
	
}
