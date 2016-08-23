package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.AttributeHierarchyNode;

public interface AttributeHierarchyNodeRepository extends PagingAndSortingRepository<AttributeHierarchyNode, Long> {
	
	AttributeHierarchyNode findChildByParentAndValue(
		@Param("parent") AttributeHierarchyNode parent,
		@Param("value") String value
	);

}
