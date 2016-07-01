package it.unibz.precise.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.rep.ModelRepository;

@RestController
@RequestMapping(FullModel.RESOURCE_NAME)
@ExposesResourceFor(Model.class)
public class FullModel implements ResourceProcessor<RepositoryLinksResource> {
	
	public static final String RESOURCE_NAME = "fullModels";

	@Autowired
	private ModelRepository repository;
	
	@Autowired
	private RepositoryEntityLinks entityLinks;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(FullModel.class).withRel(RESOURCE_NAME));
        return resource;
    }
    
    private Resource<Model> withLinks(Model model) {
		return model == null ? null : new Resource<Model>(model,
			linkTo(methodOn(FullModel.class).findOne(model.getId())).withSelfRel(),
			entityLinks.linkToSingleResource(Model.class, model.getId())
		);
    }

	private Resource<Iterable<Model>> withLinks(Iterable<Model> models) {
		return new Resource<Iterable<Model>>(models,
			linkTo(methodOn(FullModel.class).findAll()).withSelfRel(),
			entityLinks.linkToCollectionResource(Model.class)
		);
	}

	@RequestMapping(
		path="/{id}",
		method=RequestMethod.GET,
		produces={MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
	)
	public Resource<Model> findOne(@PathVariable("id") long id) {
		return withLinks(repository.findOne(id));
	}
	
	@RequestMapping(
		method=RequestMethod.GET,
		produces={MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
	)
	public Resource<Iterable<Model>> findAll() {
		return withLinks(repository.findAll());
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public HttpEntity<Resource<Model>> add(@RequestBody Model model) {
		return new ResponseEntity<>(withLinks(repository.save(model)), HttpStatus.CREATED);
	}

}

