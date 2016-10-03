package it.unibz.precise.rest;

import java.util.List;
import java.util.function.Function;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibz.precise.check.ConsistencyWarning;
import it.unibz.precise.model.BaseEntity;
import it.unibz.util.Util;

@Relation(value="warning", collectionRelation="warnings")
public class WarningResourceContent {
	
	@JsonIgnore
	private ConsistencyWarning warning;
	
	private Function<BaseEntity, ?> entityMapper;
	
	public WarningResourceContent(ConsistencyWarning warning, Function<BaseEntity, ?> entityMapper) {
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

	@JsonProperty("entities")
	public List<?> getEntities() {
		return Util.mapToList(warning.getEntities(), entityMapper::apply);
	}

}
