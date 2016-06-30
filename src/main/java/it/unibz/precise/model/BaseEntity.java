package it.unibz.precise.model;

import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	protected <T extends BaseEntity> List<T> updateList(List<T> oldList, List<T> newList) {
		if (oldList == null)
			oldList = newList;
		else {
			oldList.clear();
			oldList.addAll(newList);
		}
		return oldList;
	}
	
}
