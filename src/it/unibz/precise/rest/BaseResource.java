package it.unibz.precise.rest;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import it.unibz.precise.data.JPAUtil;
import it.unibz.precise.model.Identifiable;

@Stateless
public abstract class BaseResource<T extends Identifiable> {
	
//	@PersistenceUnit(unitName="it.unibz.precise")
//	private EntityManagerFactory entityManagerFactory;
	
	@PersistenceContext(unitName="it.unibz.precise")
	private EntityManager entityManager;
	
	private Class<T> targetClass;
	
	public BaseResource(Class<T> targetClass) {
		this.targetClass = targetClass;
	}
	
//	protected <R> R query(Function<EntityManager, R> callback) {
//		EntityManager em = entityManagerFactory.createEntityManager();
//		try {
//			return callback.apply(em);
//		} finally {
//			em.close();
//		}
//	}
	
//	protected void operation(Consumer<EntityManager> callback) {
//		EntityManager em = entityManagerFactory.createEntityManager();
//		try {
//			callback.accept(em);
//		} finally {
//			em.close();
//		}
//	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<T> getAll() {
		return JPAUtil.getAll(entityManager, targetClass);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public T getById(@PathParam("id") int id) {
		return entityManager.find(targetClass, id);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void add(T entity) {
		entityManager.persist(entity);		
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public void remove(T entity) {
		entityManager.remove(entity);
	}
	
}
