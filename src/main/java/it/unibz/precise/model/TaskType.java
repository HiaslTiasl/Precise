package it.unibz.precise.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"model_id", "name"})
})
public class TaskType extends BaseEntity {
	
	@Column(nullable=false)
	private String name;
	private String description;
	private String craft;
	
	@ManyToOne
	private Phase phase;
	
	@ManyToOne
	private Model model;
	
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

	public String getCraft() {
		return craft;
	}

	public void setCraft(String craft) {
		this.craft = craft;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	
	@Transient
	public Long getPhaseID() {
		return phase == null ? null : phase.getId();
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.TYPES.setOne(this, model);
	}
	
	void internalSetModel(Model model) {
		this.model = model;
	}

	public void checkOverlappingLocations() {}

}
