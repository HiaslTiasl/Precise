package it.unibz.precise.rest.mdl.ast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.AttributeHierarchyNode;
import it.unibz.precise.model.Location;
import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.model.OrderType;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.InvalidLocationException;
import it.unibz.precise.rest.mdl.LocationHierarchyMismatchException;
import it.unibz.precise.rest.mdl.MissingIntermediateEntryException;
import it.unibz.precise.rest.mdl.NonExistingLocationException;
import it.unibz.util.Util;

@JsonIdentityInfo(generator=IntSequenceGenerator.class, property="id", scope=MDLTaskAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskAST {
	
	@JsonIgnore
	private Task task;
	
	private MDLTaskTypeAST type;
	private boolean globalExclusiveness;
	private List<MDLAttributeAST> exclusiveness;
	private Map<String, OrderType> order;
	private List<Map<String, String>> locations;
	
	public MDLTaskAST() {
	}
	
	public MDLTaskAST(MDLFileAST context, Task task) {
		this.task = task;	
		type = context.translate(task.getType());
		globalExclusiveness = task.isGlobalExclusiveness();
		exclusiveness = Util.mapToList(task.getExclusiveness(), context::translate);
		order = Util.mapToMap(task.getOrderSpecifications(),
			os -> os.getAttribute().getName(),
			OrderSpecification::getOrderType
		);
		locations = Util.mapToList(task.getLocations(), MDLTaskAST::locationToMap);
	}
	
	public Task toTask() {
		if (task == null) {
			task = new Task();
	
			TaskType taskType = type.toTaskType();
			List<AttributeHierarchyLevel> levels = taskType.getPhase().getAttributeHierarchyLevels();
			
			task.setType(taskType);
			task.setGlobalExclusiveness(globalExclusiveness);
			task.setExclusiveness(Util.mapToList(exclusiveness, MDLAttributeAST::toAttribute));
			task.setOrderSpecifications(
				order == null ? null : levels.stream()
					.map(AttributeHierarchyLevel::getAttribute)
					.filter(a -> order.containsKey(a.getName()))
					.map(a -> new OrderSpecification(a, order.get(a.getName())))
					.collect(Collectors.toList())
			);
			task.setLocations(Util.mapToList(locations, locMap -> mapToLocation(levels, task, locMap)));
		}
		return task;
	}
	
	public MDLTaskTypeAST getType() {
		return type;
	}

	public void setType(MDLTaskTypeAST type) {
		this.type = type;
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

	public List<Map<String, String>> getLocations() {
		return locations;
	}

	public void setLocations(List<Map<String, String>> locations) {
		this.locations = locations;
	}

	public static String toLocationMapString(Map<String, String> locationMap) {
		return locationMap.entrySet().stream()
			.map(e -> e.getKey() + "=" + e.getValue())
			.collect(Collectors.joining(", ", "[", "]"));
	}
	
	public static Map<String, String> nodeToMap(AttributeHierarchyNode node) {
		Map<String, String> map = new LinkedHashMap<>();
		for (AttributeHierarchyNode n = node; n != null; n = n.getParent())
			map.put(n.getLevel().getAttribute().getName(), n.getValue());
		return map;
	}

	public static Map<String, String> locationToMap(Location location) {
		return nodeToMap(location.getNode());
	}
	
	public static Location mapToLocation(List<AttributeHierarchyLevel> levels, Task task, Map<String, String> locationMap)
		throws InvalidLocationException
	{
		int entryCount = locationMap.size();
		
		if (entryCount > levels.size())
			throw new LocationHierarchyMismatchException(locationMap);
		
		AttributeHierarchyNode parent = null;
		
		for (int i = 0; i < entryCount; i++) {
			AttributeHierarchyLevel level = levels.get(i);
			String attrString = level.getAttribute().getName();
			String value = locationMap.get(attrString);
			
			if (value == null)
				throw new MissingIntermediateEntryException(locationMap, attrString);
			
			AttributeHierarchyNode node = parent == null
				? level.findNodeByValue(value)
				: parent.findChildByValue(value);
				
			if (node == null)
				throw new NonExistingLocationException(locationMap, nodeToMap(parent), attrString, value);
			
			parent = node;
		}
			
		return new Location(parent, task);
	}
	
}
