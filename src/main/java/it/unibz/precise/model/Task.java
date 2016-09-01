package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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

import it.unibz.util.Util;

@Entity
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
	
	@PostLoad
	public void postLoad() {
		updateLocationPatterns();
		updateOrderSpecifications();
	}

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
	
	private Map<String, PatternEntry> locationToPattern(Location location) {
		return checkPattern(nodeToPattern(location.getNode()));
	}
	
	public void updateLocationPatterns() {
		this.locationPatterns = Util.mapToList(locations, this::locationToPattern);
	}
	
	public List<Map<String, PatternEntry>> getLocationPatterns() {
		return locationPatterns;
	}
	
	public void setLocationPatterns(List<Map<String, PatternEntry>> patterns) {
		this.locationPatterns = patterns;
		this.locations = patterns.stream()
			.map(this::patternToNode)
			.map(Location::new)
			.collect(Collectors.toList());
	}
	
	public void updateOrderSpecifications() {
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
	
	/** Reorder the given values according to their appearance in */
	private List<String> ordered(Attribute attribute, Collection<String> values) {
		return attribute.getRange().stream()
			.filter(values::contains)
			.collect(Collectors.toList());
	}
	
	/**
	 * Set allowed values for the given attribute in the given pattern.
	 * If the current value in the corresponding entry is not contained in the allowed values,
	 * it is set to wildcard.
	 * If the corresponding entry is null, a new one is created with a wildcard value.
	 */
	private PatternEntry setAllowedValuesToNodes(Map<String, PatternEntry> pattern, Attribute attribute, List<String> values) {
		values.add(0, PatternEntry.WILDCARD_VALUE);
		String attrName = attribute.getName();
		PatternEntry entry = pattern.get(attrName);
		boolean create = entry == null;
		// A missing entry is equivalent to one with a wildcard
		if (create) {
			entry = new PatternEntry(attrName);
			pattern.put(attrName, entry);
		}
		entry.setAllowedValues(values);
		if (!create)
			entry.checkValue();
		return entry;
	}
	
	/**
	 * Checks the given pattern in terms of allowed values.
	 * A PatternEntry with a value that is not contained in its allowed values is set to wildcard.
	 * Any missing PatternEntries are initialized as wildcards.
	 * If pattern is null, a global pattern is returned.
	 */
	public Map<String, PatternEntry> checkPattern(Map<String, PatternEntry> pattern) throws InvalidLocationException {
		if (pattern == null)
			pattern = new LinkedHashMap<String, PatternEntry>();
		List<AttributeHierarchyLevel> levels = type.getPhase().getAttributeHierarchyLevels();
		int levelCount = levels.size();
		if (levelCount > 0) {
			Map<String, AttributeHierarchyNode> tree = levels.get(0).getNodes();
			for (int i = 0; i < levelCount; i++) {
				AttributeHierarchyLevel level = levels.get(i);
				Attribute attribute = level.getAttribute();
				List<String> allowedValues = tree == null ? new ArrayList<>() : ordered(attribute, tree.keySet());
				PatternEntry entry = setAllowedValuesToNodes(pattern, attribute, allowedValues);
				String value = entry.getValue();
				if (tree != null) {
					AttributeHierarchyNode node = tree.get(value);
					tree = node == null ? null : node.getChildren();
				}
			}
		}
		return pattern;
	}
	
	public AttributeHierarchyNode patternToNode(Map<String, PatternEntry> pattern) throws InvalidLocationException {
		return patternToNode(pattern, true);
	}
	
	public AttributeHierarchyNode patternToNode(Map<String, PatternEntry> pattern, boolean strict) throws InvalidLocationException {
		List<AttributeHierarchyLevel> levels = type.getPhase().getAttributeHierarchyLevels();
		
		int entryCount = pattern.size();
		int levelCount = levels.size();
		if (entryCount > levelCount)
			throw new LocationHierarchyMismatchException(pattern);
		
		AttributeHierarchyNode parent = null;
		
		boolean encounteredWildcard = false;
		
		if (levelCount > 0) {
			Map<String, AttributeHierarchyNode> tree = levels.get(0).getNodes();
			for (int i = 0; i < entryCount; i++) {
				AttributeHierarchyLevel level = levels.get(i);
				String attrString = level.getAttribute().getName();
				PatternEntry entry = pattern.get(attrString);
				
				if (entry == null || !entry.hasValue()) {
					encounteredWildcard = true;
					tree = null;
				}
				else if (encounteredWildcard && strict)
					throw new MissingIntermediateEntryException(pattern, attrString);
				else if (tree != null) {
					String value = pattern.get(attrString).getValue();
					
					AttributeHierarchyNode node = tree.get(value);
							
					if (node != null) {
						parent = node;
						tree = node.getChildren();
					}
					else if (!strict)
						break;
					else
						throw new NonExistingLocationException(pattern, nodeToPattern(parent), attrString, value);
				}
			}
		}
			
		return parent;
	}
	
	public Map<String, PatternEntry> nodeToPattern(AttributeHierarchyNode node) {
		return AttributeHierarchyNode.toPattern(node, type.getPhase());
	}
	
}
