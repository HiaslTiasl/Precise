package it.unibz.precise.rest;

import java.util.Collection;

import javax.ws.rs.Path;

import it.unibz.precise.data.DataSources;
import it.unibz.precise.model.Model;

@Path("/model")
public class ModelResource extends BaseResource<Model> {
	
	public ModelResource() {
		super(Model.class);
	}
	
	@Override
	public Collection<Model> getAll() {
		return DataSources.getDummyData();
	}

}
