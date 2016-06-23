package it.unibz.precise.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

public class JPAUtil {
	
	public static <T> List<T> getAll(EntityManager em, Class<T> type) {
		CriteriaQuery<T> cq = em
			.getCriteriaBuilder()
			.createQuery(type);
		return em
			.createQuery(cq.select(cq.from(type)))
			.getResultList();
	}
	
}
