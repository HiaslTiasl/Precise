package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.util.Util;

@Entity
@JsonPropertyOrder(value={"type"})
@JsonIgnoreProperties(value={"manHours", "durationHours"}, allowGetters=true)
public class Task extends BaseEntity {

	public static final int DEFAULT_CREW_COUNT = 1;
	
	private static Comparator<Task> shortNameAndIDComparator = Comparator.comparing(
		(Task t) -> t.getType().getShortName()
	).thenComparing(Task::getId);
	
	public enum DurationType { MANUAL, AUTO }

	@NotNull(message="{task.type.required}")
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
	
	@Embedded
	private Scope exclusiveness = new Scope(Scope.Type.UNIT);
	
	private Integer totalQuantity;
	
	@Min(0)
	private Integer crewSize;
	
	@Min(0)
	private Integer crewCount;
	
	private DurationType durationType = DurationType.AUTO;
	
	@Min(0)
	private Integer durationDays;
	
	private Float quantityPerDay;

	private int units;
	
	@OneToMany(mappedBy="target")
	private List<Dependency> in = new ArrayList<>();
	
	@OneToMany(mappedBy="source")
	private List<Dependency> out = new ArrayList<>();

	@ManyToOne
	private Model model;

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		TaskTypeToMany.TASKS.setOne(this, type);
	}
	
	void internalSetType(TaskType type) {
		this.type = type;
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
	
	public void updateLocations() {
		locations.forEach(Location::update);
	}
	
	public void updateLocationPatterns() {
		this.locationPatterns = Util.mapToList(locations, this::locationToPattern);
	}
	
	private Map<String, PatternEntry> locationToPattern(Location location) {
		return LocationPatterns.locationToPattern(location, type.getPhase());
	}
	
	public List<Map<String, PatternEntry>> getLocationPatterns() {
		return locationPatterns;
	}
	
	public void setLocationPatterns(List<Map<String, PatternEntry>> patterns) {
		setLocationPatterns(patterns, true);
	}

	public void setLocationPatterns(List<Map<String, PatternEntry>> patterns, boolean strict) {
		if (type != null && type.getPhase() != null) {
			List<AttributeHierarchyLevel> levels = type.getPhase().getAttributeHierarchyLevels();
			this.locations = patterns.stream()
//				.map(p -> LocationPatterns.patternToNode(this, p, levels, strict))
//				.map(Location::new)
//				.collect(Collectors.toList());
				.collect(ArrayList::new, (list, p) -> {
					try {
						AttributeHierarchyNode node = LocationPatterns.patternToNode(this, p, levels, true);
						list.add(new Location(node));
					} catch (InvalidLocationException ile) {
						if (strict)
							throw ile;
					}
				}, List::addAll);
			// Only replace patterns if no exception thrown
			this.locationPatterns = patterns;
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
	
	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Integer getCrewSize() {
		return crewSize;
	}

	public void setCrewSize(Integer crewSize) {
		this.crewSize = crewSize;
	}

	public Integer getCrewCount() {
		return crewCount;
	}

	public void setCrewCount(Integer crewCount) {
		this.crewCount = crewCount;
	}

	public DurationType getDurationType() {
		return durationType;
	}

	public void setDurationType(DurationType durationType) {
		this.durationType = durationType;
	}
	
	public void updateDuration() {
		if (crewSize == null)
			crewCount = null;
		else if (crewCount == null)
			crewCount = DEFAULT_CREW_COUNT;
		Float days = exactDurationDays();
		switch (durationType) {
		case AUTO:
			durationDays = days == null ? null : (int)Math.ceil(days);
			break;
		case MANUAL:
			quantityPerDay = days == null || totalQuantity == null || !hasCrewParameters() ? null
				: totalQuantity / (crewCount * days);
			break;
		}
	}
	
	private boolean hasCrewParameters() {
		return crewSize != null && crewCount != null;
	}
	
	private boolean hasQuantities() {
		return totalQuantity != null && quantityPerDay != null;
	}
	
	private boolean hasWellDefinedDuration() {
		switch (durationType) {
		case MANUAL:
			return durationDays != null;
		case AUTO:
			return hasCrewParameters() && hasQuantities();
		default:
			return false;
		}
	}
	
	private Float exactDurationDays() {
		return !hasWellDefinedDuration() ? null
			: durationType == DurationType.MANUAL ? durationDays
			: totalQuantity / (crewCount * quantityPerDay);
	}
	
	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}
	
	public Integer getManHours() {
		Float days = exactDurationDays();
		return days == null || !hasCrewParameters() ? null
			: (int)(crewCount * crewSize * model.getHoursPerDay() * days);
	}
	
	public Float getQuantityPerDay() {
		return quantityPerDay;
	}

	public void setQuantityPerDay(Float quantityPerDay) {
		this.quantityPerDay = quantityPerDay;
	}
	
	public int getUnits() {
		return units;
	}

	public Scope getExclusiveness() {
		return exclusiveness;
	}
	
	public void updateExclusiveness() {
		if (exclusiveness == null || type.getPhase() == null)
			exclusiveness = new Scope(Scope.Type.UNIT);
		else {
			exclusiveness.update(Util.mapToList(
				type.getPhase().getAttributeHierarchyLevels(),
				AttributeHierarchyLevel::getAttribute
			), true);
		}
	}

	public void setExclusiveness(Scope exclusiveness) {
		this.exclusiveness = exclusiveness;
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
	
	public List<Dependency> getIn() {
		return in;
	}
	
	public void setIn(List<Dependency> in) {
		TaskToMany.IN_DEPENDENCIES.setMany(this, in);
	}

	void internalSetIn(List<Dependency> in) {
		this.in = in;
	}

	public List<Dependency> getOut() {
		return out;
	}

	public void setOut(List<Dependency> out) {
		TaskToMany.OUT_DEPENDENCIES.setMany(this, out);
	}

	void internalSetOut(List<Dependency> out) {
		this.out = out;
	}
	
	public String getShortIdentification() {
		return type.getShortName() + '#' + getId();
	}
	
	public static Comparator<Task> shortIdentificationComparator() {
		return shortNameAndIDComparator;
	}
	
	public void countUnits() {
		Phase phase = type.getPhase();
		units = phase == null ? 0
			: locations.stream()
				.map(Location::getNode)
				.mapToInt(loc -> loc != null ? loc.getUnits() : phase.getUnits())
				.sum();
	}
	
	@PrePersist
	@PreUpdate
	public void initDependentFields() {
		countUnits();
		updateDependentFields();
	}

	@PostLoad
	public void updateDependentFields() {
		updateLocations();
		updateLocationPatterns();
		updateExclusiveness();
		updateDuration();
	}

	@Override
	public String toString() {
		return "Task [id=" + getId() + ", type=" + type + "]";
	}
	
}
