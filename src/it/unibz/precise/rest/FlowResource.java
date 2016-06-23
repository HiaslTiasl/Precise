package it.unibz.precise.rest;

import javax.ws.rs.Path;

import it.unibz.precise.model.Flow;

@Path("/flow")
public class FlowResource extends BaseResource<Flow> {

	public FlowResource() {
		super(Flow.class);
	}

}
