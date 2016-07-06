package it.unibz.precise.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Configuration.class)
@JsonIdentityReference(alwaysAsId=false)
public class Configuration extends BaseEntity {
	
	private String name;
	@Lob
	private String description;
	
	@OneToMany(mappedBy="config", cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JsonManagedReference
	private List<TaskType> taskTypes;
	
	@OneToMany(mappedBy="config", cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JsonManagedReference
	private List<ConstructionUnit> constructionUnits;
	
	public Configuration() {
	}
	
	public Configuration(List<TaskType> taskTypes, List<ConstructionUnit> units) {
		this.taskTypes = taskTypes;
		this.constructionUnits = units;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<TaskType> getTaskTypes() {
		return taskTypes;
	}

	public void setTaskTypes(List<TaskType> taskTypes) {
		this.taskTypes = taskTypes;
	}
	
	public List<ConstructionUnit> getConstructionUnits() {
		return constructionUnits;
	}
	
	public void setConstructionUnits(List<ConstructionUnit> units) {
		this.constructionUnits = updateList(constructionUnits, units);
	}

}
