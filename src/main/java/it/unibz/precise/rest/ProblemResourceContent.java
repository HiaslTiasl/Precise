package it.unibz.precise.rest;

import java.util.List;
import java.util.function.Function;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibz.precise.check.ModelProblem;
import it.unibz.precise.check.ProblemClassification.Category;
import it.unibz.precise.model.BaseEntity;
import it.unibz.util.Util;

/**
 * Representation of {@link ModelProblem}s as returned to the client.
 * Needed for being able to convert entities and locations
 * into a structure without cycles (among object references), because
 * otherwise a {@link StackOverflowError} occurs on serialization.
 * This is done by a {@link Function} provided to the constructor.
 * Conversion of locations instead is fully handled by this class.
 * 
 * @author MatthiasP
 *
 */
@Relation(value="problem", collectionRelation="problems")
public class ProblemResourceContent {
	
	@JsonIgnore
	private ModelProblem problem;
	
	private Function<BaseEntity, ?> entityMapper;
	
	/** Create a {@code WarningResourceContent} for the given warning, using the given specified {@code entityMapper}. */
	public ProblemResourceContent(ModelProblem problem, Function<BaseEntity, ?> entityMapper) {
		this.problem = problem;
		this.entityMapper = entityMapper;
	}
	
	@JsonProperty("category")
	public Category getCategory() {
		return problem.getCategory();
	}

	@JsonProperty("type")
	public String getType() {
		return problem.getType();
	}

	@JsonProperty("message")
	public String getMessage() {
		return problem.getMessage();
	}

	/** Returns the entities transformed by the entity mapper. */
	@JsonProperty("entities")
	public List<?> getEntities() {
		return Util.mapToList(problem.getEntities(), entityMapper::apply);
	}
	
	/** Returns the locations transformed into {@link LocationContent}s. */
	@JsonProperty("locations")
	public List<?> getLocations() {
		return Util.mapToList(
			problem.getLocations(),
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
