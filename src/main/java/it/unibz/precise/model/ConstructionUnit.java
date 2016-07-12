package it.unibz.precise.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=ConstructionUnit.class)
@JsonIdentityReference(alwaysAsId=false)
@JsonIgnoreProperties("casection")
public class ConstructionUnit extends BaseEntity implements ModelComponent {
	
	@Embedded
	@JsonUnwrapped
	private CASection caSection;
	
	private Integer unit;
	
	@ManyToOne
	private Model model;
	
	public ConstructionUnit() {
	}
	
	public ConstructionUnit(CASection caSection, Integer unit) {
		this.caSection = caSection;
		this.unit = unit;
	}

	public CASection getCASection() {
		return caSection;
	}

	public void setCASection(CASection caSection) {
		this.caSection = caSection;
	}

	public Integer getUnit() {
		return unit;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
}
