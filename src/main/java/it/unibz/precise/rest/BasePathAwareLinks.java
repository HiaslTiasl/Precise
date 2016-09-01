package it.unibz.precise.rest;

import static org.springframework.hateoas.mvc.BasicLinkBuilder.linkToCurrentMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.stereotype.Service;

@Service
public class BasePathAwareLinks {
	
	@Autowired
	private RepositoryRestConfiguration config;
	
	public LinkBuilder underBasePath(Object arg) {
		return linkToCurrentMapping()
			.slash(config.getBasePath())
			.slash(arg);
	}
	
}
