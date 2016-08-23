package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class Task extends BaseEntity {

	@ManyToOne
	private TaskType type;
	
	@Embedded
	private Position position;

	@OneToMany(mappedBy="task", cascade=CascadeType.ALL)
	private List<Location> locations = new ArrayList<>();
	
	@OneToMany(mappedBy="task", cascade=CascadeType.ALL)
	private List<OrderSpecification> orderSpecifications = new ArrayList<>();
	
	@ManyToMany
	private List<Attribute> exclusiveness = new ArrayList<>();
	
	private float numberOfWorkersNeeded;
	
	private float numberOfUnitsPerDay;
	
	private boolean globalExclusiveness;

	@ManyToOne
	private Model model;

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}
	
	@Transient
	public Long getTypeID() {
		return type == null ? null : type.getId();
	}
	
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		TaskToMany.LOCATIONS.setMany(this, locations);
	}
	
	public void addLocation(Location location) {
		TaskToMany.LOCATIONS.addOneOfMany(this, location);
	}
	
	void internalSetLocations(List<Location> locations) {
		this.locations = locations;
	}

	public List<OrderSpecification> getOrderSpecifications() {
		return orderSpecifications;
	}

	public void setOrderSpecifications(List<OrderSpecification> orderSpecifications) {
		TaskToMany.ORDER_SPECIFICATIONS.setMany(this, orderSpecifications);
	}
	
	public void addOrderSpecification(OrderSpecification orderSpecification) {
		TaskToMany.ORDER_SPECIFICATIONS.addOneOfMany(this, orderSpecification);
	}
	
	void internalSetOrderSpecifications(List<OrderSpecification> orderSpecifications) {
		this.orderSpecifications = orderSpecifications;
	}
	
	public float getNumberOfWorkersNeeded() {
		return numberOfWorkersNeeded;
	}

	public void setNumberOfWorkersNeeded(float numberOfWorkersNeeded) {
		this.numberOfWorkersNeeded = numberOfWorkersNeeded;
	}

	public float getNumberOfUnitsPerDay() {
		return numberOfUnitsPerDay;
	}

	public void setNumberOfUnitsPerDay(float numberOfUnitsPerDay) {
		this.numberOfUnitsPerDay = numberOfUnitsPerDay;
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
