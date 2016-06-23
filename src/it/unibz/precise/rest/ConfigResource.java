package it.unibz.precise.rest;

import javax.ws.rs.Path;

import it.unibz.precise.model.Configuration;

@Path("/config")
public class ConfigResource extends BaseResource<Configuration> {

	public ConfigResource() {
		super(Configuration.class);
	}

}
