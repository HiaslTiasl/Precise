package it.unibz.precise.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import it.unibz.precise.model.HasLongId;
import it.unibz.precise.model.Model;

/**
 * Custom {@link ResourceProcessor} for {@link Model} or a projection thereof.
 * 
 * @author MatthiasP
 *
 * @param <T> The type represented by the resources (either {@link Model} or a projection thereof).
 */
public abstract class AbstractModelResourceProcessor<T extends HasLongId> implements ResourceProcessor<Resource<T>> {
	
	@Autowired
	private BasePathAwareLinks service;

	@Override
	public Resource<T> process(Resource<T> resource) {
		long id = resource.getContent().getId();
		resource.add(
			service.underBasePath(
				linkTo(methodOn(ProblemsController.class).getProblems(id))
			)
			.withRel("problems")
		);
		resource.add(
			service.underBasePath(
				linkTo(methodOn(PitchComputationController.class).computePitches(id, null))
			)
			.withRel("pitches")
		);
		return resource;
	}

}
