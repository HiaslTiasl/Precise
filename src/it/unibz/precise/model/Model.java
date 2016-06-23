package it.unibz.precise.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

@Entity
@JsonIdentityInfo(property="id", generator=IntSequenceGenerator.class, scope=Model.class)
@JsonIdentityReference(alwaysAsId=false)
public class Model implements Identifiable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	private Configuration config;
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	private Flow flow;
	
	public Model() {
	}
	
	public Model(Configuration config, Flow flow) {
		this.config = config;
		this.flow = flow;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
