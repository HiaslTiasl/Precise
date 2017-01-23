package it.unibz.precise.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class of all persistent entities.
 * 
 * @author MatthiasP
 *
 */
@MappedSuperclass
public abstract class BaseEntity implements HasLongId {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
}
