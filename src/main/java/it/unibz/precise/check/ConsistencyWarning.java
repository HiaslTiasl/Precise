package it.unibz.precise.check;

import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.Task;

import java.util.List;

public class ConsistencyWarning implements ConsistencyClassification {
	
	private Category category;
	
	private String type;
	
	private String message;
	
	private List<? extends BaseEntity> entities;
	
	private List<TaskLocation> locations;
	
	public ConsistencyWarning(Category category, String type, String message, List<? extends BaseEntity> entities, List<TaskLocation> locations) {
		this.category = category;
		this.type = type;
		this.message = message;
		this.entities = entities;
		this.locations = locations;
	}

	public Category getCategory() {
		return category;
	}

	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public List<? extends BaseEntity> getEntities() {
		return entities;
	}
	
	public List<TaskLocation> getLocations() {
		return locations;
	}
	
	public static class TaskLocation {
		private Task task;
		private Location location;
		private int index;
		
		public TaskLocation(Task task, Location location, int index) {
			this.task = task;
			this.location = location;
			this.index = index;
		}
		
		public Task getTask() {
			return task;
		}
		
		public Location getLocation() {
			return location;
		}

		public int getIndex() {
			return index;
		}
		
	}

}
