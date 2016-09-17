package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.util.Util;

@Entity
@JsonPropertyOrder(value={"type"})
public class Task extends BaseEntity {

	@ManyToOne
	private TaskType type;
	
	@Embedded
	private Position position;

	@ElementCollection
	private List<Location> locations = new ArrayList<>();
	
	@Transient
	private List<Map<String, PatternEntry>> locationPatterns = new ArrayList<>();
	
	@ElementCollection
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
		updateOrderSpecifications();
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

	@JsonIgnore
	public List<Location> getLocations() {
		return locations;
	}

	@JsonIgnore
	public void setLocations(List<Location> locations) {
		this.locations = locations;
		updateLocationPatterns();
	}
	
	public void addLocation(Location location) {
		locations.add(location);
		locationPatterns.add(locationToPattern(location));
	}
	
	public void updateLocationPatterns() {
		this.locationPatterns = Util.mapToList(locations, this::locationToPattern);
	}
	
	private Map<String, PatternEntry> locationToPattern(Location location) {
		return LocationPatterns.locationToPattern(location, type.getPhase().getAttributeHierarchyLevels());
	}
	
	public List<Map<String, PatternEntry>> getLocationPatterns() {
		return locationPatterns;
	}
	
	public void setLocationPatterns(List<Map<String, PatternEntry>> patterns) {
		List<AttributeHierarchyLevel> levels = type.getPhase().getAttributeHierarchyLevels();
		this.locations = patterns.stream()
			.map(p -> LocationPatterns.patternToNode(p, levels))
			.map(Location::new)
			.collect(Collectors.toList());
		// Only add pattern if it is a valid location
		this.locationPatterns = patterns;
	}
	
	public void updateOrderSpecifications() {
		if (type == null)
			orderSpecifications = new ArrayList<>();
		else {
			List<AttributeHierarchyLevel> levels = type.getPhase().getAttributeHierarchyLevels();
			Map<Attribute, OrderSpecification> orderSpecMap = Util.mapToMap(orderSpecifications,
				OrderSpecification::getAttribute,
				Function.identity()
			);
			orderSpecifications = levels.stream()
				.map(AttributeHierarchyLevel::getAttribute)
				.map(a -> orderSpecMap.computeIfAbsent(a, OrderSpecification::new))
				.collect(Collectors.toList());
		}
	}
	
	public List<OrderSpecification> getOrderSpecifications() {
		return orderSpecifications;
	}

	public void setOrderSpecifications(List<OrderSpecification> orderSpecifications) {
		this.orderSpecifications = orderSpecifications;
	}
	
	public void addOrderSpecification(OrderSpecification orderSpecification) {
		orderSpecifications.add(orderSpecification);
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
	
	@PostLoad
	public void updateDependentFields() {
		updateLocationPatterns();
		updateOrderSpecifications();
	}
	
}
