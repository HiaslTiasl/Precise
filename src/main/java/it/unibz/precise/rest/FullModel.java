package it.unibz.precise.rest;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.rep.ModelRepository;

@RestController()
@RequestMapping("fullModel")
public class FullModel implements ResourceProcessor<RepositoryLinksResource> {

	@Autowired
	private ModelRepository repository;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(FullModel.class).withRel("fullModel"));
        return resource;
    }
	
	@RequestMapping(
		path="{id}",
		method=RequestMethod.GET,
		produces={MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
	)
	public Model findOne(@PathParam("id") long id) {
		return repository.findOne(id);
	}
	
	@RequestMapping(
		method=RequestMethod.GET,
		produces={MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
	)
	public Iterable<Model> findAll() {
		return repository.findAll();
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public Model add(@RequestBody Model model) {
		return repository.save(model);
	}

}
