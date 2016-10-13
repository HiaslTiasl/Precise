package it.unibz.precise.rest.mdl.conversion;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.OrderSpecification;
import it.unibz.precise.model.OrderType;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;
import it.unibz.util.Util;

class TaskTranslator extends AbstractMDLTranslator<Task, MDLTaskAST> {
	
	TaskTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Task task, MDLTaskAST mdlTask) {
		TaskType taskType = task.getType();
		mdlTask.setDefinition(context().taskTypes().toMDL(taskType));
		mdlTask.setNumberOfWorkersNeeded(task.getNumberOfWorkersNeeded());
		mdlTask.setDurationDays(task.getDurationDays());
		mdlTask.setExclusiveness(context().scopes().toMDL(task.getExclusiveness()));
		mdlTask.setOrder(Util.mapToMap(task.getOrderSpecifications(),
			os -> os.getAttribute().getName(),
			OrderSpecification::getOrderType
		));
		mdlTask.setPosition(task.getPosition());
		mdlTask.setLocations(Util.mapToList(task.getLocationPatterns(), this::toSimplePattern));
	}
	
	@Override
	public void updateEntity(MDLTaskAST mdlTask, Task task) {
		
		TaskType taskType = context().taskTypes().toEntity(mdlTask.getDefinition());
		Phase phase = taskType.getPhase();
		Map<String, OrderType> order = mdlTask.getOrder();
		
		task.setType(taskType);
		task.setNumberOfWorkersNeeded(mdlTask.getNumberOfWorkersNeeded());
		task.setDurationDays(mdlTask.getDurationDays());
		task.setExclusiveness(context().scopes().toEntity(mdlTask.getExclusiveness()));
		task.setOrderSpecifications(
				order == null ? null : phase.getAttributeHierarchyLevels().stream()
				.map(AttributeHierarchyLevel::getAttribute)
				.filter(a -> order.containsKey(a.getName()))
				.map(a -> new OrderSpecification(a, order.get(a.getName())))
				.collect(Collectors.toList())
		);
		task.setPosition(mdlTask.getPosition());
		task.setLocationPatterns(Util.mapToList(mdlTask.getLocations(), p -> toPattern(p, phase.getAttributeHierarchyLevels())));
	}
	
	@Override
	public Task createEntity(MDLTaskAST mdl) {
		return new Task();
	}

	@Override
	public MDLTaskAST createMDL(Task entity) {
		return new MDLTaskAST();
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
