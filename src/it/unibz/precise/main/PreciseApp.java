package it.unibz.precise.main;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import it.unibz.precise.rest.ConfigResource;
import it.unibz.precise.rest.FlowResource;
import it.unibz.precise.rest.ModelResource;

@ApplicationPath("/rest/*")
public class PreciseApp extends ResourceConfig {
	
	public PreciseApp() {
		register(ConfigResource.class);
		register(FlowResource.class);
		register(ModelResource.class);
		register(JacksonFeature.class);
		register(JacksonProvider.class);
	}
	
}
