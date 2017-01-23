package it.unibz.precise.check;

import it.unibz.precise.model.BaseEntity;

import it.unibz.precise.model.Location;
import it.unibz.precise.model.Task;

import java.util.List;

/**
 * Represents an (immutable) consistency problem in a model.
 * Holds a textual description of the problem as well as information needed to highlight the
 * problematic parts in the diagram.
 * 
 * @author MatthiasP
 *
 */
public class ConsistencyWarning implements ConsistencyClassification {
	
	// Classification
	private Category category;
	private String type;

	// Info
	private String message;
	private List<? extends BaseEntity> entities;
	private List<TaskLocation> locations;
	
	/** Create a ConsistencyWarning. */
	protected ConsistencyWarning(Category category, String type, String message, List<? extends BaseEntity> entities, List<TaskLocation> locations) {
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

	/** Presistent entities involved in the problem. */
	public List<? extends BaseEntity> getEntities() {
		return entities;
	}
	
	/** Locations involved in the problem. */
	public List<TaskLocation> getLocations() {
		return locations;
	}
	
	/** Represents a location of a particular task. */
	public static class TaskLocation {
		
		private Task task;
		private Location location;
		private int index;
		
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
