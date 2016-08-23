package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"model_id", "name"})
})
public class Phase extends BaseEntity {

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	@OneToMany(mappedBy="phase", cascade=CascadeType.ALL)
	@OrderBy("position")
	private List<AttributeHierarchyLevel> attributeHierarchyLevels = new ArrayList<>();
	
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

	public List<AttributeHierarchyLevel> getAttributeHierarchyLevels() {
		return attributeHierarchyLevels;
	}

	public void setAttributeHierarchyLevels(List<AttributeHierarchyLevel> attributeHierarchyLevels) {
		PhaseToMany.LEVELS.setMany(this, attributeHierarchyLevels);
	}
	
	public void addAttribute(Attribute attribute) {
		addLevel(new AttributeHierarchyLevel(this, attribute));
	}

	public void addLevel(AttributeHierarchyLevel level) {
		level.setPosition(attributeHierarchyLevels.size() + 1);
		PhaseToMany.LEVELS.addOneOfMany(this, level);
	}
	
	void internalSetLevels(List<AttributeHierarchyLevel> levels) {
		attributeHierarchyLevels = levels;
	}
	
	void internalAddLevel(AttributeHierarchyLevel level) {
		attributeHierarchyLevels.add(level);
		level.setPosition(attributeHierarchyLevels.size());
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.PHASES.setOne(this, model);
	}
	
	void internalSetModel(Model model) {
		this.model = model;
	}
	
	public AttributeHierarchyLevel nextLevel(AttributeHierarchyLevel prev) {
		int nextIndex = prev == null ? 0 : prev.getPosition();		// N.B. position == index + 1
		return nextIndex < attributeHierarchyLevels.size()
			? attributeHierarchyLevels.get(nextIndex) : null;
	}

}
