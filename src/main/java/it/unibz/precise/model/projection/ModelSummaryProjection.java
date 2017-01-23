package it.unibz.precise.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.HasLongId;
import it.unibz.precise.model.Model;

/**
 * Model with direct properties and state info only.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="modelSummary", types=Model.class)
public interface ModelSummaryProjection extends HasLongId  {

	String getName();
	
	String getDescription();
	
	int getHoursPerDay();

	Model.State getState();
	
	@Value("#{target.state.configInfo}")
	Model.PartInfo getConfigInfo();
	
	@Value("#{target.state.diagramInfo}")
	Model.PartInfo getDiagramInfo();
	
}
