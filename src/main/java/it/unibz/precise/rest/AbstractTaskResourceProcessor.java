package it.unibz.precise.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import it.unibz.precise.model.HasLongId;
import it.unibz.precise.model.Task;

/**
 * Custom {@link ResourceProcessor} for {@link Task} or a projection thereof.
 * 
 * @author MatthiasP
 *
 * @param <T> The type represented by the resources (either {@link Task} or a projection thereof).
 */
public abstract class AbstractTaskResourceProcessor<T extends HasLongId> implements ResourceProcessor<Resource<T>> {
	
	@Autowired
	private BasePathAwareLinks service;
	
	@Override
	public Resource<T> process(Resource<T> resource) {
		long id = resource.getContent().getId();
		resource.add(
			service.underBasePath(
				linkTo(methodOn(CheckedPatternController.class).checkedPatternForTask(id, new HashMap<>()))
			)
			.withRel("checkedPattern")
		);
		return resource;
	}
	
}
