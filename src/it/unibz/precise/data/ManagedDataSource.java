package it.unibz.precise.data;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.ManagedType;

import it.unibz.precise.model.Identifiable;

public class ManagedDataSource implements DataSource {
	
	@PersistenceContext
	EntityManager entityManager;
	

	@Override
	public Collection<Class<?>> getClasses() {
		return (entityManager.getMetamodel().getManagedTypes().stream()
			.map(ManagedType::getJavaType)
			.collect(Collectors.toList()));
	}

	@Override
	public <T extends Identifiable> Collection<T> getAll(Class<T> type) {
		CriteriaQuery<T> cq = entityManager
			.getCriteriaBuilder()
			.createQuery(type);
		
		return entityManager
			.createQuery(cq.select(cq.from(type)))
			.getResultList();
	}

	@Override
	public <T extends Identifiable> T getById(Class<T> type, int id) {
		return entityManager.find(type, id);
	}

	@Override
	public <T extends Identifiable> void add(Class<T> type, T entity) {
		entityManager.persist(entity);
	}

	@Override
	public <T extends Identifiable> void remove(Class<T> type, T entity) {
		entityManager.remove(entity);
	}

}
