package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Task;

/**
 * Task with pitch parameters and resulting man-hours only.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="pitch", types=Task.class)
public interface PitchProjection {

	Pitch getPitch();
	
	int getManHours();
	
}
