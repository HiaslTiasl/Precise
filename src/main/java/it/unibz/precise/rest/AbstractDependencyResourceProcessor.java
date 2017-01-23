package it.unibz.precise.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.HasLongId;

/**
 * Custom {@link ResourceProcessor} for {@link Dependency} or a projection thereof.
 * 
 * @author MatthiasP
 *
 * @param <T> The type represented by the resources (either {@link Dependency} or a projection thereof).
 */
public abstract class AbstractDependencyResourceProcessor<T extends HasLongId> implements ResourceProcessor<Resource<T>> {
	
	@Autowired
	private BasePathAwareLinks service;
	
	@Override
	public Resource<T> process(Resource<T> resource) {
		long id = resource.getContent().getId();
		resource.add(
			service.underBasePath(
				linkTo(methodOn(DependencyAttributesController.class).getAllowedAttributes(id, null))
			)
			.withRel("attributes")
		);
		return resource;
	}

}
