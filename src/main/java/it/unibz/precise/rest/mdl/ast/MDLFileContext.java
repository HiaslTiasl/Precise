package it.unibz.precise.rest.mdl.ast;

import java.util.function.Function;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.util.Util;

public class MDLFileContext {
	
	private static final boolean CONCURRENT_TRANSLATORS = false;

	private transient Function<Attribute, MDLAttributeAST> attributeTranslator;
	private transient Function<Phase, MDLPhaseAST> phaseTranslator;
	private transient Function<TaskType, MDLTaskTypeAST> taskTypeTranslator;
	private transient Function<Task, MDLTaskAST> taskTranslator;
	private transient Function<Dependency, MDLDependencyAST> dependencyTranslator;
	
	public MDLAttributeAST translate(Attribute attribute) {
		if (attributeTranslator == null)
			attributeTranslator = Util.memoize(a -> new MDLAttributeAST(this, a), CONCURRENT_TRANSLATORS);
		return attributeTranslator.apply(attribute);
	}
	
	public MDLPhaseAST translate(Phase phase) {
		if (phaseTranslator == null)
			phaseTranslator = Util.memoize(p -> new MDLPhaseAST(this, p), CONCURRENT_TRANSLATORS);
		return phaseTranslator.apply(phase);
	}
	
	public MDLTaskTypeAST translate(TaskType taskType) {
		if (taskTypeTranslator == null)
			taskTypeTranslator = Util.memoize(tt -> new MDLTaskTypeAST(this, tt), CONCURRENT_TRANSLATORS);
		return taskTypeTranslator.apply(taskType);
	}
	
	public MDLTaskAST translate(Task task) {
		if (taskTranslator == null)
			taskTranslator = Util.memoize(t -> new MDLTaskAST(this, t), CONCURRENT_TRANSLATORS);
		return taskTranslator.apply(task);
	}
	
	public MDLDependencyAST translate(Dependency dependency) {
		if (dependencyTranslator == null)
			dependencyTranslator = Util.memoize(d -> new MDLDependencyAST(this, d), CONCURRENT_TRANSLATORS);
		return dependencyTranslator.apply(dependency);
	}
	
}
