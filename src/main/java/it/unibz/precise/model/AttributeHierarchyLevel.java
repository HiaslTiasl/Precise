package it.unibz.precise.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Represents a level in the CA hierarchy of a phase.
 * A {@code AttributeHierarchyLevel} has a phase, an attribute, contains several {@link AttributeHierarchyNode}s.
 * 
 * @author MatthiasP
 *
 */
@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(name=AttributeHierarchyLevel.UC_PHASE_ATTRIBUTE, columnNames={"phase_id", "attribute_id"}),
	@UniqueConstraint(name=AttributeHierarchyLevel.UC_PHASE_POSITION, columnNames={"phase_id", "position"})
})
public class AttributeHierarchyLevel extends BaseEntity {
	
	public static final String UC_PHASE_ATTRIBUTE = "UC_LEVEL_PHASE_ATTRIBUTE";
	public static final String UC_PHASE_POSITION = "UC_LEVEL_PHASE_POSITION";

	@ManyToOne
	private Phase phase;			// The phase this CA hierarchy corresponds to
	
	@ManyToOne
	private Attribute attribute;	// The attribute that values at this level correspond to
	
	private int position;			// The position of this level (attribute) in the hierarchy (phase), starting from 1.
	
	@OneToMany(mappedBy="level", cascade=CascadeType.ALL, orphanRemoval=true)
	@MapKey(name="value")
	private Map<String, AttributeHierarchyNode> nodes = new HashMap<>();
	
	public AttributeHierarchyLevel() {
	}
	
	public AttributeHierarchyLevel(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		PhaseToMany.LEVELS.setOne(this, phase);
	}
	
	void internalSetPhase(Phase phase) {
		this.phase = phase;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/** Indicates whether this level is the leaf level in the hierarchy. */
	public boolean isUnit() {
		return position == phase.getAttributeHierarchyLevels().size();
	}
	
	/** Indicates whether the next level is the leaf level in the hierarchy. */
	public boolean hasOnlyUnits() {
		return position == phase.getAttributeHierarchyLevels().size() - 1;
	}

	public Map<String, AttributeHierarchyNode> getNodes() {
		return nodes;
	}

	public void setNodes(Map<String, AttributeHierarchyNode> nodes) {
		LevelToMany.NODES.setMany(this, nodes);
	}
	
	public void addNode(AttributeHierarchyNode node) {
		LevelToMany.NODES.addOneOfMany(this, node);
	}
	
	void internalSetNodes(Map<String, AttributeHierarchyNode> nodes) {
		this.nodes = nodes;
	}
	
	public AttributeHierarchyLevel next() {
		return phase.nextLevel(this);
	}

	@Override
	public String toString() {
		return "AttributeHierarchyLevel [id=" + getId() + ", phase=" + phase + ", attribute=" + attribute + "]";
	}
	
}
