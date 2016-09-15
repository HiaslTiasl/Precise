package it.unibz.precise.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.rep.DependencyRepository;
import it.unibz.util.Util;

@RepositoryRestController
public class DependencyAttributesController {
	
	@Autowired
	private DependencyRepository repository;
	
	@RequestMapping(path="/dependencies/{id}/attributes", method=RequestMethod.GET)
	public ResponseEntity<Resources<PersistentEntityResource>> getAllowedAttributes(
		@PathVariable("id") long id,
		PersistentEntityResourceAssembler resourceAssembler)
	{
		Dependency dependency = repository.findOne(id);
		return dependency == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
			: new ResponseEntity<>(
				new Resources<>(Util.mapToList(dependency.getAttributes(), resourceAssembler::toResource)),
				HttpStatus.OK
			);
	}
	

}
