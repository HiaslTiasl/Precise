package it.unibz.precise.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

@Entity
@JsonIdentityInfo(property="id", generator=IntSequenceGenerator.class, scope=Configuration.class)
@JsonIdentityReference(alwaysAsId=false)
public class Configuration implements Identifiable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;
	
	@OneToMany(mappedBy="config", cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinColumn(name="owner", referencedColumnName="id")
	private List<TaskType> taskTypes;
	
	@OneToMany(mappedBy="config", cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinColumn(name="owner", referencedColumnName="id")
	private List<ConstructionUnit> constructionUnits;
	
	public Configuration() {
	}
	
	public Configuration(List<TaskType> taskTypes, List<ConstructionUnit> units) {
		this.taskTypes = taskTypes;
		this.constructionUnits = units;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
		this.constructionUnits = units;
	}

}
