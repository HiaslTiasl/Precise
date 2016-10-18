package it.unibz.precise.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.unibz.precise.model.Scope.Type;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"source_id", "target_id"})
})
public class Dependency extends BaseEntity {
	
	@Embeddable
	public static class LabelPosition {
		
		@Column(nullable=true)
		private Float distance;
		@Column(nullable=true)
		private Float offset;
		
		public Float getDistance() {
			return distance;
		}
		
		public void setDistance(Float distance) {
			this.distance = distance;
		}
		
		public Float getOffset() {
			return offset;
		}
		
		public void setOffset(Float offset) {
			this.offset = offset;
		}
	}
	
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
	
	@Embedded
	private LabelPosition labelPosition;
	
	@ElementCollection
	private List<Position> vertices;
	
	@Embedded
	private Scope scope = new Scope(Type.GLOBAL);
	
	@ManyToOne
	private Model model;

	public Task getSource() {
		return source;
	}

	public void setSource(Task source) {
		TaskToMany.OUT_DEPENDENCIES.setOne(this, source);
	}
	
	public void internalSetSource(Task source) {
		this.source = source;
	}

	public Task getTarget() {
		return target;
	}

	public void setTarget(Task target) {
		TaskToMany.IN_DEPENDENCIES.setOne(this, target);
	}
	
	void internalSetTarget(Task target) {
		this.target = target;
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
	
	public LabelPosition getLabelPosition() {
		return labelPosition;
	}

	public void setLabelPosition(LabelPosition labelPosition) {
		this.labelPosition = labelPosition;
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
	
	public void updateSourceVertex() {
		if (source != null && sourceVertex != null)
			sourceVertex = null;
	}

	public Position getTargetVertex() {
		return targetVertex;
	}

	public void setTargetVertex(Position targetVertex) {
		this.targetVertex = targetVertex;
	}
	
	public void updateTargetVertex() {
		if (target != null && targetVertex != null)
			targetVertex = null;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
	public boolean canHaveUnitScope() {
		return source == null || target == null
			|| source.getType().getPhase() == target.getType().getPhase();
	}
	
	public void updateScope() {
		if (scope == null)
			scope = new Scope(Scope.Type.GLOBAL);
		else {
			scope.update(getAttributes());
			if (scope.getType() == Type.UNIT && !canHaveUnitScope())
				scope.setType(Type.ATTRIBUTES);
		}
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
		boolean noSourceAttrs = sourceAttrs == null;
		boolean noTargetAttrs = targetAttrs == null;
		
		return noSourceAttrs && noTargetAttrs ? Collections.emptyList()
			: noSourceAttrs ? targetAttrs.collect(Collectors.toList())
			: noTargetAttrs ? sourceAttrs.collect(Collectors.toList())
			: sourceAttrs.filter(
				targetAttrs.collect(Collectors.toSet())::contains
			)
			.collect(Collectors.toList());
	}
	
	@PostLoad
	@PrePersist
	@PreUpdate
	public void updateDependentFields() {
		updateSourceVertex();
		updateTargetVertex();
		updateScope();
	}
	
}
