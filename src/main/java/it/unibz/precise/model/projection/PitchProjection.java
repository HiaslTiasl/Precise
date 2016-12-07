package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Task;

@Projection(name="pitch", types=Task.class)
public interface PitchProjection {

	Pitch getPitch();
	
	int getManHours();
	
}
