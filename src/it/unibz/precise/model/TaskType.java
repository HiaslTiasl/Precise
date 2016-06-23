package it.unibz.precise.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

@Entity
@JsonIdentityInfo(property="id", generator=IntSequenceGenerator.class, scope=TaskType.class)
@JsonIdentityReference(alwaysAsId=false)
public class TaskType implements Identifiable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;
	private String name;
	
	@ManyToOne
	private Configuration config;

	public TaskType() {
	}

	public TaskType(String name) {
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

}
