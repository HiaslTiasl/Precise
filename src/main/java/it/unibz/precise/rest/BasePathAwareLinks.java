package it.unibz.precise.rest;

import java.net.URI;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.support.BaseUriLinkBuilder;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

@Service
public class BasePathAwareLinks {
	
	private final URI contextBaseURI;
	private final URI restBaseURI;
	
	@Autowired
	public BasePathAwareLinks(ServletContext servletContext, RepositoryRestConfiguration config) {
		contextBaseURI = URI.create(servletContext.getContextPath());
		restBaseURI = config.getBasePath();
	}

	public LinkBuilder underBasePath(ControllerLinkBuilder linkBuilder) {
		return BaseUriLinkBuilder.create(contextBaseURI)
			.slash(restBaseURI)
			.slash(contextBaseURI.relativize(URI.create(linkBuilder.toUri().getPath())));
	}

}
