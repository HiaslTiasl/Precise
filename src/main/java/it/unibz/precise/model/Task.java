package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class Task extends BaseEntity {

	@ManyToOne
	private TaskType type;
	
	@ElementCollection
	private List<Location> locations = new ArrayList<>();
	
	@ElementCollection
	private List<OrderSpecification> orderSpecifications = new ArrayList<>();
	
	@ManyToMany
	private List<Attribute> exclusiveness = new ArrayList<>();
	
	private boolean globalExclusiveness;

	@ManyToOne
	private Model model;

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public List<OrderSpecification> getOrderSpecifications() {
		return orderSpecifications;
	}

	public void setOrderSpecifications(List<OrderSpecification> orderSpecifications) {
		this.orderSpecifications = orderSpecifications;
	}
	
	public List<Attribute> getExclusiveness() {
		return exclusiveness;
	}

	public void setExclusiveness(List<Attribute> exclusiveness) {
		this.exclusiveness = exclusiveness;
	}

	public boolean isGlobalExclusiveness() {
		return globalExclusiveness;
	}

	public void setGlobalExclusiveness(boolean globalExclusiveness) {
		this.globalExclusiveness = globalExclusiveness;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.TASKS.setOne(this, model);
	}

	void internalSetModel(Model model) {
		this.model = model;
	}
	
}
