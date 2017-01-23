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
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.util.Util;

/**
 * Represents an activity to be executed in a set of locations.
 * Corresponds to a box in the diagram.
 * The activity is determined by the {@link TaskType}, together with the
 * corresponding {@link Phase} and the required {@link Craft}.
 * 
 * Further contains {@link Pitch} parameters, a {@link Position} in the diagram,
 * and a {@link Scope} of the task's exclusiveness. 
 * 
 * @author MatthiasP
 *
 */
@Entity
@JsonPropertyOrder(value={"type"})
@JsonIgnoreProperties(value={"manHours", "durationHours"}, allowGetters=true)
public class Task extends BaseEntity {

	public static final int DEFAULT_CREW_COUNT = 1;
	
	private static Comparator<Task> shortNameAndIDComparator = Comparator.comparing(
		(Task t) -> t.getType().getShortName()
	).thenComparing(Task::getId);
	
	@NotNull(message="{task.type.required}")
	@ManyToOne
	private TaskType type;
	
	@Embedded
	private Position position;

	// Use pattern representation in JSON representation instead
	@JsonIgnore		
	@ElementCollection
	private List<Location> locations = new ArrayList<>();
	
	@Transient
	private List<Map<String, PatternEntry>> locationPatterns = new ArrayList<>();		// pattern representation of locations
	
	@ElementCollection
	private List<OrderSpecification> orderSpecifications = new ArrayList<>();
	
	@Embedded
	private Scope exclusiveness = new Scope(Scope.Type.UNIT);
	
	@Embedded
	private Pitch pitch;
	
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

	public Pitch getPitch() {
		return pitch;
	}

	public void setPitch(Pitch pitch) {
		this.pitch = pitch;
	}
	
	/** Returns man-hours based on pitch parameters and working hours per day. */
	public int getManHours() {
		return (int)Math.ceil(model.getHoursPerDay() * pitch.exactManDays());
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
		updateLocationPatterns();							// Reflect changes in patterns
	}
	
	public void addLocation(Location location) {
		locations.add(location);
		locationPatterns.add(locationToPattern(location));	// Reflect changes in patterns
	}
	
	/** Update locations. */
	public void updateLocations() {
		locations.forEach(Location::update);
		updateLocationPatterns();							// Reflect changes in patterns
	}
	
	/** Update location patterns based on locations. */
	public void updateLocationPatterns() {
		this.locationPatterns = Util.mapToList(locations, this::locationToPattern);
	}
	
	/** Convert the given location to a pattern representation using this task's phase. */
	private Map<String, PatternEntry> locationToPattern(Location location) {
		return LocationPatterns.locationToPattern(location, type.getPhase());
	}
	
	public List<Map<String, PatternEntry>> getLocationPatterns() {
		return locationPatterns;
	}
	
	public void setLocationPatterns(List<Map<String, PatternEntry>> patterns) {
		setLocationPatterns(patterns, true);
	}

	/**
	 * Set the given location patterns.
	 * @param patterns The location patterns to be set
	 * @param strict Indicates whether all patterns must be valid.
	 *        If true, an exception is thrown on the first invalid pattern.
	 *        Otherwise, only valid patterns are used.
	 * @throws InvalidLocationException if {@code strict} is set and at least one pattern
	 *         is invalid.
	 */
	public void setLocationPatterns(List<Map<String, PatternEntry>> patterns, boolean strict) {
		if (type != null && type.getPhase() != null) {
			List<AttributeHierarchyLevel> levels = type.getPhase().getAttributeHierarchyLevels();
			this.locations = patterns.stream()
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
	
	public int getUnits() {
		return units;
	}

	public Scope getExclusiveness() {
		return exclusiveness;
	}
	
	/** Update the scope of exclusiveness */
	public void updateExclusiveness() {
		if (exclusiveness == null || type.getPhase() == null)
			exclusiveness = new Scope(Scope.Type.UNIT);
		else {
			exclusiveness.update(Util.mapToList(
				type.getPhase().getAttributeHierarchyLevels(),
				AttributeHierarchyLevel::getAttribute
			));
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
	
	/** Returns a textual identification consisting of the definitinon's short name and the ID. */
	public String getShortIdentification() {
		return type.getShortName() + '#' + getId();
	}
	
	/** Compares two tasks by shortName first and then by ID. */
	public static Comparator<Task> shortIdentificationComparator() {
		return shortNameAndIDComparator;
	}
	
	/** Updates the number of units contained in locations of the task. */
	public void countUnits() {
		Phase phase = type.getPhase();
		// N.B. We assume that the number of units in CAs does not change,
		// so no need to recompute those.
		// Also, we assume that locations are non-overlapping here.
		units = phase == null ? 0		// No phase -> no locations -> no units
			: locations.stream()
				.map(Location::getNode)
				.mapToInt(loc -> loc != null ? loc.getUnits() : phase.getUnits())
				.sum();
	}
	
	/** Initialize fields that depend on other fields. */
	@PrePersist
	@PreUpdate
	public void initDependentFields() {
		countUnits();
		updateDependentFields();
	}

	/** Update fields that depend on other fields. */
	@PostLoad
	public void updateDependentFields() {
		updateLocations();
		updateExclusiveness();
		pitch.update();
	}

	@Override
	public String toString() {
		return "Task [id=" + getId() + ", type=" + type + "]";
	}
	
}
