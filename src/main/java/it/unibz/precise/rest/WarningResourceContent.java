package it.unibz.precise.rest;

import java.util.List;
import java.util.function.Function;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibz.precise.check.ModelProblem;
import it.unibz.precise.model.BaseEntity;
import it.unibz.util.Util;

/**
 * Representation of warnings as returned to the client.
 * Needed for being able to convert entities and locations
 * into another representation that does not have cycles.
 * This is done by a {@link Function} provided to the constructor.
 * Conversion of locations instead is fully handled by this class.
 * 
 * @author MatthiasP
 *
 */
@Relation(value="warning", collectionRelation="warnings")
public class WarningResourceContent {
	
	@JsonIgnore
	private ModelProblem warning;
	
	private Function<BaseEntity, ?> entityMapper;
	
	/** Create a {@code WarningResourceContent} for the given warning, using the given specified {@code entityMapper}. */
	public WarningResourceContent(ModelProblem warning, Function<BaseEntity, ?> entityMapper) {
		this.warning = warning;
		this.entityMapper = entityMapper;
	}

	@JsonProperty("type")
	public String getType() {
		return warning.getType();
	}

	@JsonProperty("message")
	public String getMessage() {
		return warning.getMessage();
	}

	/** Returns the entities transformed by the entity mapper. */
	@JsonProperty("entities")
	public List<?> getEntities() {
		return Util.mapToList(warning.getEntities(), entityMapper::apply);
	}
	
	/** Returns the locations transformed into {@link LocationContent}s. */
	@JsonProperty("locations")
	public List<?> getLocations() {
		return Util.mapToList(
			warning.getLocations(),
			l -> new LocationContent(entityMapper.apply(l.getTask()), l.getIndex())
		);
	}
	
	/**
	 * Representation of locations as an index within the corresponding task.
	 * The task is expected to be transformed by an entity mapper before being passed.
	 */
	public static class LocationContent {
		private Object task;
		private int index;
		
		public LocationContent(Object task, int index) {
			this.task = task;
			this.index = index;
		}

		public Object getTask() {
			return task;
		}
		
		public int getIndex() {
			return index;
		}
	}

}
