package it.unibz.precise.check;

import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.TaskType;

import java.util.Set;

/**
 * Represents an immutable node in a disjunctive graph that corresponds to a process model.
 * Each node has a task type and a unit location.
 * Overrides {@link Object#equals(Object) equals} and {@link Object#hashCode() hashCode}
 * using the underlying task type and unit such that any pair of {@link TaskType} and
 * {@link AttributeHierarchyNode}{@link Set} is contained at most once in a {@link Set},
 * which is important when translating a {@link Model} into a {@link DisjunctiveGraph}.
 * 
 * @see ModelToGraphTranslator
 * @see DisjunctiveGraph
 */
public class TaskUnitNode {
	private final TaskType taskType;
	private final AttributeHierarchyNode unit;
	
	public TaskUnitNode(TaskType taskType, AttributeHierarchyNode unit) {
		this.taskType = taskType;
		this.unit = unit;
	}
	
	public TaskType getTaskType() {
		return taskType;
	}

	public AttributeHierarchyNode getUnit() {
		return unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taskType == null) ? 0 : taskType.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskUnitNode other = (TaskUnitNode) obj;
		if (taskType == null) {
			if (other.taskType != null)
				return false;
		} else if (!taskType.equals(other.taskType))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "<" + taskType.getShortName() + "," + PatternEntry.toValueString(unit.getPattern()) + ">"; 
	}
	
}