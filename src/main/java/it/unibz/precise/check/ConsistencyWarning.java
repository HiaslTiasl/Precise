package it.unibz.precise.check;

import java.util.List;

import it.unibz.precise.model.BaseEntity;

public class ConsistencyWarning {
	
	private String type;
	
	private String message;
	
	private List<? extends BaseEntity> entities;
	
	public ConsistencyWarning(String type, String message, List<? extends BaseEntity> entities) {
		this.type = type;
		this.message = message;
		this.entities = entities;
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

}
