package it.unibz.precise.model.projection;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Phase;

@Projection(name="phaseSummary", types=Phase.class)
public interface PhaseSummaryProjection {
	
	String getName();
	
	@Value("#{target.attributeHierarchyLevels.![attribute]}")
	List<AttributeSummaryProjection> getAttributes();

}
