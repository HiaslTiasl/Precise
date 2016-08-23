package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"phase_id", "attribute_id"}),
	@UniqueConstraint(columnNames={"phase_id", "position"})
})
public class AttributeHierarchyLevel extends BaseEntity implements Ordered {

	@ManyToOne
	private Phase phase;
	
	@ManyToOne
	private Attribute attribute;
	
	private int position;
	
	@OneToMany(mappedBy="level", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<AttributeHierarchyNode> nodes = new ArrayList<>();
	
	public AttributeHierarchyLevel() {
	}

	public AttributeHierarchyLevel(Phase phase, Attribute attribute) {
		this.phase = phase;
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

	@Transient
	public Long getPhaseID() {
		return phase == null ? null : phase.getId();
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

	public List<AttributeHierarchyNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<AttributeHierarchyNode> nodes) {
		LevelToMany.NODES.setMany(this, nodes);
	}
	
	void internalSetNodes(List<AttributeHierarchyNode> nodes) {
		this.nodes = nodes;
	}
	
	public void addNode(AttributeHierarchyNode node) {
		LevelToMany.NODES.addOneOfMany(this, node);
	}
	
	void internalAddNode(AttributeHierarchyNode node) {
		nodes.add(node);
	}
	
	public AttributeHierarchyNode findNodeByValue(String value) {
		return AttributeHierarchyNode.findByValue(nodes, value);
	}

}
