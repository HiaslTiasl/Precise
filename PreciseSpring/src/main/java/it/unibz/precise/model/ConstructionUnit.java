package it.unibz.precise.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=ConstructionUnit.class)
@JsonIdentityReference(alwaysAsId=false)
public class ConstructionUnit extends BaseEntity {
	
	private String sector;
	private String level;
	//private String orientation;
	private String section;
	
	private Integer unit;
	
	@ManyToOne
	@JsonBackReference
	private Configuration config;
	
	public ConstructionUnit() {
	}
	
	public ConstructionUnit(String sector, String level, String section, Integer unit) {
		this.sector = sector;
		this.level = level;
		this.section = section;
		this.unit = unit;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public Integer getUnit() {
		return unit;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return "ConstructionUnit [sector=" + sector + ", level=" + level + ", section=" + section + ", unit=" + unit
				+ "]";
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}
	
}
