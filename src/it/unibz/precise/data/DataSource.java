package it.unibz.precise.data;

import java.util.Collection;

import it.unibz.precise.model.Identifiable;

public interface DataSource {
	
	Collection<Class<?>> getClasses();
	<T extends Identifiable> Collection<T> getAll(Class<T> type);
	<T extends Identifiable> T getById(Class<T> type, int id);
	<T extends Identifiable> void add(Class<T> type, T obj);
	<T extends Identifiable> void remove(Class<T> type, T obj);
	
	default <T extends Identifiable> void addAll(Class<T> type, Collection<T> objs) {
		for (T o : objs)
			add(type, o);
	}

}
