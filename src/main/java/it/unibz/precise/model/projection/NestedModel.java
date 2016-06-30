package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Configuration;
import it.unibz.precise.model.Flow;
import it.unibz.precise.model.Model;

@Projection(name="nested", types=Model.class)
public interface NestedModel {
	Configuration getConfig();
	Flow getFlow();
}
