package it.unibz.precise.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Model;

@Projection(name="modelSummary", types=Model.class)
public interface ModelSummaryProjection {

	
	
	String getName();
	
	String getDescription();

	Model.State getState();
	
	@Value("#{target.state.configInfo}")
	Model.PartInfo getConfigInfo();
	
	@Value("#{target.state.diagramInfo}")
	Model.PartInfo getDiagramInfo();
	
}
