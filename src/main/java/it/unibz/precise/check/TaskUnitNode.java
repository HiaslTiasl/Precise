package it.unibz.precise.check;

import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Activity;

import static java.util.Comparator.comparing;

import java.util.Comparator;
import java.util.Set;

/**
 * Represents an immutable node in a disjunctive graph that corresponds to a process model.
 * Each node has an activity and a unit location.
 * Overrides {@link Object#equals(Object) equals} and {@link Object#hashCode() hashCode}
 * using the underlying activity and unit such that any pair of {@link Activity} and
 * {@link AttributeHierarchyNode}{@link Set} is contained at most once in a {@link Set},
 * which is important when translating a {@link Model} into a {@link DisjunctiveGraph}.
 * 
 * @see ModelToGraphTranslator
 * @see DisjunctiveGraph
 */
public class TaskUnitNode {
	
	public static final Comparator<TaskUnitNode> BY_TO_STRING = comparing(TaskUnitNode::toString);
	
	private final Activity activity;
	private final AttributeHierarchyNode unit;
	
	private String string;
	
	public TaskUnitNode(Activity activity, AttributeHierarchyNode unit) {
		this.activity = activity;
		this.unit = unit;
	}
	
	public Activity getActivity() {
		return activity;
	}

	public AttributeHierarchyNode getUnit() {
		return unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity == null) ? 0 : activity.hashCode());
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
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
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
		if (string == null)
			string = "<" + activity.getShortName() + "," + PatternEntry.toValueString(unit.getPattern()) + ">";
		return string;
	}
	
}