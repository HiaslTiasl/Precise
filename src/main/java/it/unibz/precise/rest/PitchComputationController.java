package it.unibz.precise.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.unibz.precise.model.InconsistentPitchException;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.projection.PitchProjection;
import it.unibz.precise.rep.ModelRepository;

/**
 * Exposes {@link Pitch}es and allows check them for consistency,
 * compute missing parameters, and compute the resulting man-hours.
 * 
 * @author MatthiasP
 *
 */
@RepositoryRestController
public class PitchComputationController {
	
	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private ProjectionFactory projectionFactory;
	
	@RequestMapping(
		path="/tasks/{id}/pitches",
		method={RequestMethod.PUT, RequestMethod.PATCH}
	)
	public ResponseEntity<?> computePitches(
		@PathVariable("id") long id,
		@RequestBody Pitch pitch
	) {
		
		Model model = repository.findOne(id);
		if (!pitch.update()) {
			// The pitch is inconsistent -> throw exception
			throw new InconsistentPitchException();
		}
		// Here we know the pitch is consistent
		// -> send it back together with the resulting man-hours,
		Task task = new Task();
		task.setModel(model);
		task.setPitch(pitch);
		
		// Project the task to a PitchProjection such that only the pitch and the man-hours are included
		return ResponseEntity.ok(projectionFactory.createProjection(PitchProjection.class, task));
	}
	
}
