package it.unibz.precise.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"source_id", "target_id"})
})
public class Dependency extends BaseEntity {
	
	private boolean alternate;
	private boolean chain;

	@ManyToOne
	private Task source;
	
	@ManyToOne
	private Task target;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="x", column=@Column(name="source_x")),
		@AttributeOverride(name="y", column=@Column(name="source_y"))
	})
	private Position sourceVertex;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="x", column=@Column(name="target_x")),
		@AttributeOverride(name="y", column=@Column(name="target_y"))
	})
	private Position targetVertex;
	
	@ElementCollection
	private List<Position> vertices;
	
	@ManyToMany
	private List<Attribute> scope;
	
	private boolean globalScope;
	
	@ManyToOne
	private Model model;

	public Task getSource() {
		return source;
	}

	public void setSource(Task source) {
		this.source = source;
	}
	
	@Transient
	public Long getSourceID() {
		return source == null ? null : source.getId();
	}

	public Task getTarget() {
		return target;
	}

	public void setTarget(Task target) {
		this.target = target;
	}
	
	@Transient
	public Long getTargetID() {
		return target == null ? null : target.getId();
	}

	public boolean isAlternate() {
		return alternate;
	}

	public void setAlternate(boolean alternate) {
		this.alternate = alternate;
	}

	public boolean isChain() {
		return chain;
	}

	public void setChain(boolean chain) {
		this.chain = chain;
	}
	
	public List<Position> getVertices() {
		return vertices;
	}
	
	public void setVertices(List<Position> vertices) {
		this.vertices = vertices;
	}

	public Position getSourceVertex() {
		return sourceVertex;
	}

	public void setSourceVertex(Position sourceVertex) {
		this.sourceVertex = sourceVertex;
	}

	public Position getTargetVertex() {
		return targetVertex;
	}

	public void setTargetVertex(Position targetVertex) {
		this.targetVertex = targetVertex;
	}

	public List<Attribute> getScope() {
		return scope;
	}

	public void setScope(List<Attribute> scope) {
		this.scope = scope;
	}

	public boolean isGlobalScope() {
		return globalScope;
	}

	public void setGlobalScope(boolean globalScope) {
		this.globalScope = globalScope;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.DEPENDENCIES.setOne(this, model);
	}

	void internalSetModel(Model model) {
		this.model = model;
	}
	
	private static Stream<Attribute> attributesOf(Task task) {
		return task == null ? null
			: task.getType().getPhase().getAttributeHierarchyLevels().stream()
				.map(AttributeHierarchyLevel::getAttribute);
	}
	
	@Transient
	@JsonIgnore
	public List<Attribute> getAttributes() {
		Stream<Attribute> sourceAttrs = attributesOf(source);
		Stream<Attribute> targetAttrs = attributesOf(target);
		return (sourceAttrs == null ? targetAttrs
			: targetAttrs == null ? sourceAttrs
			: sourceAttrs.filter(targetAttrs.collect(Collectors.toSet())::contains)
		).collect(Collectors.toList());
	}
	
	@PrePersist
	public void prePersist() {
		if (source != null && sourceVertex != null)
			sourceVertex = null;
		if (target != null && targetVertex != null)
			targetVertex = null;
	}
	
}
