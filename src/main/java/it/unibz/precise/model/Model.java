package it.unibz.precise.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Model.class)
@JsonIdentityReference(alwaysAsId=false)
public class Model extends BaseEntity {
	
	private String name;

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	private Configuration config;
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	private Flow flow;
	
	public Model() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Model(Configuration config, Flow flow) {
		this.config = config;
		this.flow = flow;
	}
	
	public Configuration getConfig() {
		return config;
	}

	public Flow getFlow() {
		return flow;
	}
	
	public void setConfig(Configuration config) {
		this.config = config;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}
	
}
