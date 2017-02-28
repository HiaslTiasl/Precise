package it.unibz.precise.check;

import java.util.Collection;

import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Task;

/**
 * Represents an (immutable) problem in a model.
 * Holds a textual description of the problem as well as information needed to highlight the
 * problematic parts in the diagram.
 * 
 * @author MatthiasP
 *
 */
public class ModelProblem implements ProblemClassification {
	
	// Classification
	private Category category;
	private String type;

	// Problem info
	private String message;
	private Collection<? extends BaseEntity> entities;
	private Collection<TaskLocation> locations;
	
	/** Create a ConsistencyWarning. */
	protected ModelProblem(Category category, String type, String message, Collection<? extends BaseEntity> entities, Collection<TaskLocation> locations) {
		this.category = category;
		this.type = type;
		this.message = message;
		this.entities = entities;
		this.locations = locations;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public String getType() {
		return type;
	}

	/** Descriptive message to be shown to the user. */
	public String getMessage() {
		return message;
	}

	/** Persistent entities involved in the problem. */
	public Collection<? extends BaseEntity> getEntities() {
		return entities;
	}
	
	/** Locations involved in the problem. */
	public Collection<TaskLocation> getLocations() {
		return locations;
	}
	
	/** Represents a location of a particular task. */
	public static class TaskLocation {
		
		private Task task;
		private Location location;
		private int index;
		
		/** Create a TaskLocation. */
		public TaskLocation(Task task, int index) {
			this(task, task.getLocations().get(index), index);
		}
		
		/** Create a TaskLocation. */
		public TaskLocation(Task task, Location location, int index) {
			this.task = task;
			this.location = location;
			this.index = index;
		}
		
		/** The task containing the location. */
		public Task getTask() {
			return task;
		}
		
		/** The location itself. */
		public Location getLocation() {
			return location;
		}

		/** The index of the location in the containing task. */
		public int getIndex() {
			return index;
		}
		
	}

}