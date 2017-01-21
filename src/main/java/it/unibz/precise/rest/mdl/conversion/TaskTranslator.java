package it.unibz.precise.rest.mdl.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.AttributeHierarchyLevel;
import it.unibz.precise.model.InvalidTaskException;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Scope.Type;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.ast.MDLOrderSpecificationAST;
import it.unibz.precise.rest.mdl.ast.MDLScopeAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;
import it.unibz.util.Util;

class TaskTranslator extends AbstractMDLTranslator<Task, MDLTaskAST> {
	
	TaskTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Task task, MDLTaskAST mdlTask) {
		mdlTask.setDefinition(context().taskTypes().toMDL(task.getType()));
		mdlTask.setPosition(task.getPosition());
		mdlTask.setPitch(task.getPitch());
		mdlTask.setExclusiveness(context().scopes().toMDL(task.getExclusiveness()));
		mdlTask.setOrder(Util.mapToList(task.getOrderSpecifications(), context().orderSpecs()::toMDL));
		mdlTask.setLocations(Util.mapToList(task.getLocationPatterns(), this::toSimplePattern));
	}
	
	@Override
	protected void updateEntityImpl(MDLTaskAST mdlTask, Task task) {
		TaskType taskType = context().taskTypes().toEntity(mdlTask.getDefinition());
		task.setType(taskType);
		task.setPosition(mdlTask.getPosition());
		task.setPitch(mdlTask.getPitch());
		
		Phase phase = taskType.getPhase();
		boolean strict = context().isStrictMode();
		MDLScopeAST exclusiveness = mdlTask.getExclusiveness();
		List<MDLOrderSpecificationAST> mdlOrder = mdlTask.getOrder();
		if (phase != null) {
			task.setLocationPatterns(
				Util.mapToList(mdlTask.getLocations(), p -> toPattern(p, taskType.getPhase().getAttributeHierarchyLevels())),
				strict
			);
			task.setExclusiveness(context().scopes().toEntity(exclusiveness));
			task.setOrderSpecifications(
				mdlOrder == null ? null
				: mdlOrder.stream()
					.map(context().orderSpecs()::toEntity)
					.filter(os -> os != null && os.getAttribute() != null)
					.collect(Collectors.toList())
			);
		}
		else if (!strict) {
			task.setLocationPatterns(new ArrayList<>());
			task.setExclusiveness(new Scope(Type.UNIT));
			task.setOrderSpecifications(new ArrayList<>());
		}
		else {
			if (Util.size(mdlTask.getLocations()) > 0) 
				throw new InvalidTaskException(task, "Cannot specify locations without referring to a phase");
			if (exclusiveness != null && Util.size(exclusiveness.getAttributes()) > 0) 
				throw new InvalidTaskException(task, "Cannot specify exclusiveness without referring to a phase");
			if (Util.size(mdlTask.getOrder()) > 0)
				throw new InvalidTaskException(task, "Cannot specify ordering without referring to a phase");
		}
	}
	
	@Override
	public Task createEntity() {
		return new Task();
	}

	@Override
	public MDLTaskAST createMDL() {
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
