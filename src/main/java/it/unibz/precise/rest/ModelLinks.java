package it.unibz.precise.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Service;

@Service
public class ModelLinks {
	
	@Autowired
	private BasePathAwareLinks service;
	
	public <T extends ResourceSupport> T withCustomLinks(T resource, Function<T, Long> getID) {
		long id = getID.apply(resource);
		resource.add(
			service.underBasePath(
				linkTo(methodOn(WarningsController.class).getWarnings(id, null))
			)
			.withRel("warnings")
		);
		return resource;
	}

}
