package it.unibz.precise.rest;

import java.net.URI;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.support.BaseUriLinkBuilder;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

/**
 * Workaround for a known bug that {@link ControllerLinkBuilder} does not take into account
 * the base path of REST repositories.
 * 
 * @author MatthiasP
 * @see <a href="https://github.com/spring-projects/spring-hateoas/issues/434">
 * 	https://github.com/spring-projects/spring-hateoas/issues/434
 * </>
 * @see <a href="https://jira.spring.io/browse/DATAREST-972">
 * 	https://jira.spring.io/browse/DATAREST-972
 * </>
 */
@Service
public class BasePathAwareLinks {
	
	private final URI contextBaseURI;	// Base URI of the whole application
	private final URI restBasePath;		// Base URI of Spring Data REST resources
	
	@Autowired
	public BasePathAwareLinks(ServletContext servletContext, RepositoryRestConfiguration config) {
		contextBaseURI = URI.create(servletContext.getContextPath());
		restBasePath = config.getBasePath();
	}

	public LinkBuilder underBasePath(ControllerLinkBuilder linkBuilder) {
		// context / base / linkBuilder without context
		return BaseUriLinkBuilder.create(contextBaseURI)
			.slash(restBasePath)
			.slash(contextBaseURI.relativize(URI.create(linkBuilder.toUri().getPath())));
	}

}
