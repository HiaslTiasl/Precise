package it.unibz.precise.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class TaskConstructionUnit extends BaseEntity {

	@ManyToOne
	private Task task;
	@ManyToOne
	private ConstructionUnit constructionUnit;
	private int position;
	
	public TaskConstructionUnit() {
	}
	
	public TaskConstructionUnit(Task task, ConstructionUnit constructionUnit, int position) {
		super();
		this.task = task;
		this.constructionUnit = constructionUnit;
		this.position = position;
	}
	
	public static List<TaskConstructionUnit> setPositionToIndex(List<TaskConstructionUnit> tcuList) {
		IntStream.range(0, tcuList.size())
			.forEach(i -> tcuList.get(i).setPosition(i));
		return tcuList;
	}
	
	public List<TaskConstructionUnit> createList(Task task, List<ConstructionUnit> constructionUnits) {
		return IntStream.range(0, constructionUnits.size())
			.mapToObj(i -> new TaskConstructionUnit(task, constructionUnits.get(i), i))
			.collect(Collectors.toList());
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public ConstructionUnit getConstructionUnit() {
		return constructionUnit;
	}

	public void setConstructionUnit(ConstructionUnit constructionUnit) {
		this.constructionUnit = constructionUnit;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
}
