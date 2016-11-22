package it.unibz.precise.model;

import java.util.List;
import java.util.Set;
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
import it.unibz.precise.model.validation.WellDefinedScope;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"source_id", "target_id"})
})
@WellDefinedScope
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
		Phase sourcePhase = source == null ? null : source.getType().getPhase();
		Phase targetPhase = target == null ? null : target.getType().getPhase();
		return sourcePhase == null || targetPhase == null
			|| sourcePhase.equals(targetPhase);
	}
	
	public void updateScope() {
		if (scope == null)
			scope = new Scope(Scope.Type.GLOBAL);
		else {
			scope.update(getAllowedAttributes(), true);
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
		Phase phase = task == null ? null : task.getType().getPhase();
		return phase == null ? Stream.empty()
			: phase.getAttributeHierarchyLevels().stream()
				.map(AttributeHierarchyLevel::getAttribute);
	}
	
	@Transient
	@JsonIgnore
	public List<Attribute> getAllowedAttributes() {
		Stream<Attribute> sourceAttrs = attributesOf(source);
		Stream<Attribute> targetAttrs = attributesOf(target);
		
		return sourceAttrs.filter(targetAttrs.collect(Collectors.toSet())::contains)
			.collect(Collectors.toList());
	}
	
	@Transient
	@JsonIgnore
	public Stream<Attribute> getNotAllowedScopeAttributes() {
		if (scope == null || scope.getAttributes() == null)
			return Stream.empty();
		Set<Attribute> sourceAttrs = attributesOf(source).collect(Collectors.toSet());
		Set<Attribute> targetAttrs = attributesOf(target).collect(Collectors.toSet());
		return scope.getAttributes().stream()
			.filter(a -> !sourceAttrs.contains(a) || !targetAttrs.contains(a));
	}
	
	public boolean removeNotAllowedScopeAttributes() {
		return scope.getAttributes()
			.removeAll(getNotAllowedScopeAttributes().collect(Collectors.toSet()));
	}
	
	@PostLoad
	@PrePersist
	@PreUpdate
	public void updateDependentFields() {
		updateSourceVertex();
		updateTargetVertex();
		updateScope();
	}

	@Override
	public String toString() {
		return "Dependency [id=" + getId() + ", source=" + source + ", target=" + target
				+ ", alternate=" + alternate + ", chain=" + chain + ", scope=" + scope + "]";
	}
	
}
