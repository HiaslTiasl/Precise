package it.unibz.precise.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.UnaryConstraint;
import it.unibz.precise.model.UnaryKind;

@JsonIdentityInfo(generator=PropertyGenerator.class, property="id", scope=ConstrainedTask.class)
public class ConstrainedTask {

	private long id;
	@JsonUnwrapped
	@JsonIgnoreProperties("taskType")
	private Task task;
	private String type;
	private Scope exclusivenessScope;
	@JsonProperty("constructionUnits")
	private List<ConstructionUnitRange> cuRanges;
	
	public ConstrainedTask() {
	}

	public ConstrainedTask(Task task, Scope exclusivenessScope, List<ConstructionUnitRange> constructionUnits) {
		this.task = task;
		this.exclusivenessScope = exclusivenessScope;
		this.cuRanges = constructionUnits;
	}
	
	public static ConstrainedTask fromTask(Task task) {
		return new ConstrainedTask(task, Scope.UNIT, ConstructionUnitRange.from(task.getTaskConstructionUnits()));
	}
	
	public Task toTask() {
		task.setTaskConstructionUnits(ConstructionUnitRange.resolveAll(cuRanges, task));
		return task;
	}
	
	public UnaryConstraint toUnaryConstraint() {
		return exclusivenessScope == Scope.UNIT ? null
			: new UnaryConstraint(UnaryKind.EXCLUSIVE_EXISTENCE, exclusivenessScope, task);
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@JsonProperty
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Scope getExclusivenessScope() {
		return exclusivenessScope;
	}
	
	public void setExclusivenessScope(Scope exclusivenessScope) {
		this.exclusivenessScope = exclusivenessScope;
	}

	public List<ConstructionUnitRange> getCURanges() {
		return cuRanges;
	}

	public void setCURanges(List<ConstructionUnitRange> cuRanges) {
		this.cuRanges = cuRanges;
	}
	
}
