package it.unibz.precise.rest.mdl.ast;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.model.OrderType;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.util.Util;

@JsonIdentityInfo(generator=IntSequenceGenerator.class, property="id", scope=MDLTaskAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskAST {
	
	@JsonIgnore
	private Task task;
	
	private MDLTaskTypeAST type;
	private float numberOfWorkersNeeded;
	private float numberOfUnitsPerDay;
	private boolean globalExclusiveness;
	private List<MDLAttributeAST> exclusiveness;
	private Map<String, OrderType> order;
	private Position position;
	private List<Map<String, String>> locations;
	
	public MDLTaskAST() {
	}
	
	public MDLTaskAST(MDLFileAST context, Task task) {
		this.task = task;
		TaskType taskType = task.getType();
		type = context.translate(taskType);
		numberOfWorkersNeeded = task.getNumberOfWorkersNeeded();
		numberOfUnitsPerDay = task.getNumberOfUnitsPerDay();
		globalExclusiveness = task.isGlobalExclusiveness();
		exclusiveness = Util.mapToList(task.getExclusiveness(), context::translate);
		order = Util.mapToMap(task.getOrderSpecifications(),
			os -> os.getAttribute().getName(),
			OrderSpecification::getOrderType
		);
		position = task.getPosition();
		locations = Util.mapToList(task.getLocationPatterns(), this::toSimplePattern);
	}
	
	public Task toTask() {
		if (task == null) {
			task = new Task();
	
			TaskType taskType = type.toTaskType();
			Phase phase = taskType.getPhase();
			
			task.setType(taskType);
			task.setNumberOfWorkersNeeded(numberOfWorkersNeeded);
			task.setNumberOfUnitsPerDay(numberOfUnitsPerDay);
			task.setGlobalExclusiveness(globalExclusiveness);
			task.setExclusiveness(Util.mapToList(exclusiveness, MDLAttributeAST::toAttribute));
			task.setOrderSpecifications(
				order == null ? null : phase.getAttributeHierarchyLevels().stream()
					.map(AttributeHierarchyLevel::getAttribute)
					.filter(a -> order.containsKey(a.getName()))
					.map(a -> new OrderSpecification(a, order.get(a.getName())))
					.collect(Collectors.toList())
			);
			task.setPosition(position);
			task.setLocationPatterns(Util.mapToList(locations, p -> toPattern(p, phase.getAttributeHierarchyLevels())));
		}
		return task;
	}
	
	public MDLTaskTypeAST getType() {
		return type;
	}

	public void setType(MDLTaskTypeAST type) {
		this.type = type;
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

	public boolean isGlobalExclusiveness() {
		return globalExclusiveness;
	}

	public void setGlobalExclusiveness(boolean globalExclusiveness) {
		this.globalExclusiveness = globalExclusiveness;
	}

	public List<MDLAttributeAST> getExclusiveness() {
		return exclusiveness;
	}

	public void setExclusiveness(List<MDLAttributeAST> exclusiveness) {
		this.exclusiveness = exclusiveness;
	}

	public Map<String, OrderType> getOrder() {
		return order;
	}

	public void setOrder(Map<String, OrderType> order) {
		this.order = order;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<Map<String, String>> getLocations() {
		return locations;
	}

	public void setLocations(List<Map<String, String>> locations) {
		this.locations = locations;
	}

	private Map<String, String> toSimplePattern(Map<String, PatternEntry> pattern) {
		return pattern.values().stream()
			.collect(Collectors.toMap(
				PatternEntry::getAttributeName,
				PatternEntry::getValue
			));
	}

	private Map<String, PatternEntry> toPattern(Map<String, String> simplePattern, List<AttributeHierarchyLevel> levels) {
		return levels.stream()
			.map(AttributeHierarchyLevel::getAttribute)
			.map(Attribute::getName)
			.collect(Collectors.toMap(
				Function.identity(),
				a -> new PatternEntry(a, simplePattern.get(a))
			));
	}
	
}
