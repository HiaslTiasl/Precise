package it.unibz.precise.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unibz.precise.rest.rep.ModelRepository;

@RestController
@RequestMapping(UserSyntaxController.RESOURCE_NAME)
@ExposesResourceFor(ModelDTO.class)
public class UserSyntaxController {
	
	@Autowired
	private ModelRepository repository;
	
	public static final String RESOURCE_NAME = "userSyntax";
	
	@RequestMapping(
		path="/{id}",
		method=RequestMethod.GET,
		produces={MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE}
	)
	public ModelDTO findOne(@PathVariable("id") long id) {
		return new ModelDTO(repository.findOne(id));
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public void save(@RequestBody ModelDTO modelDTO) {
		System.out.println("got something!");
		repository.save(modelDTO.toModel());
	}
	
}
