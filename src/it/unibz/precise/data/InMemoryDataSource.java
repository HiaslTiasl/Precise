package it.unibz.precise.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import it.unibz.precise.model.Configuration;
import it.unibz.precise.model.Flow;
import it.unibz.precise.model.Identifiable;
import it.unibz.precise.model.Model;

public class InMemoryDataSource implements DataSource {
	
	private Map<Long, Configuration> configs;
	private Map<Long, Flow> flows;
	private Map<Long, Model> models;
	
	private Map<Class<?>, Map<Long, ? extends Identifiable>> typeMap; 
	
	public InMemoryDataSource() {
		typeMap = new HashMap<>();
		bind(Configuration.class, configs);
		bind(Flow.class, flows);
		bind(Model.class, models);
		
		configs = new HashMap<>(); 
		flows = new HashMap<>(); 
		models = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Identifiable> Map<Long, T> bind(Class<T> type, Map<Long, T> map) {
		return (Map<Long, T>)typeMap.put(type, map);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Identifiable> Map<Long, T> mapOf(Class<T> type) {
		return (Map<Long, T>)typeMap.get(type);
	}
	
	protected <T extends Identifiable, I, O> O operation(Class<T> type, Function<Map<Long, T>, O> func) {
		Map<Long, T> map = mapOf(type);
		return map == null ? null : func.apply(map);
	}

	@Override
	public <T extends Identifiable> Collection<T> getAll(Class<T> type) {
		return operation(type, Map::values);
	}

	@Override
	public <T extends Identifiable> T getById(Class<T> type, int id) {
		return operation(type, map -> map.get(id));
	}

	@Override
	public <T extends Identifiable> void add(Class<T> type, T entity) {
		operation(type, map -> map.put(entity.getId(), entity));
	}

	@Override
	public <T extends Identifiable> void remove(Class<T> type, T entity) {
		operation(type, map -> map.remove(entity.getId(), entity));
	}

	@Override
	public Collection<Class<?>> getClasses() {
		return typeMap.keySet();
	}
	
}
