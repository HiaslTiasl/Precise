package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLDependencyAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskTypeAST;

public class MDLContext {
	
	private static final boolean CONCURRENT_TRANSLATORS = false;
	
	private final MDLTranslator<Model, MDLFileAST> fileTranslator;
	private final MDLTranslator<Model, MDLConfigAST> configTranslator;
	private final MDLTranslator<Attribute, MDLAttributeAST> attributeTranslator;
	private final MDLTranslator<Phase, MDLPhaseAST> phaseTranslator;
	private final MDLTranslator<TaskType, MDLTaskTypeAST> taskTypeTranslator;
	private final MDLTranslator<Task, MDLTaskAST> taskTranslator;
	private final MDLTranslator<Dependency, MDLDependencyAST> dependencyTranslator;
	
	public MDLContext() {
		fileTranslator       = cache(new FileTranslator(this));
		configTranslator     = cache(new ConfigTranslator(this));
		attributeTranslator  = cache(new AttributeTranslator(this));
		phaseTranslator      = cache(new PhaseTranslator(this));
		taskTypeTranslator   = cache(new TaskTypeTranslator(this));
		taskTranslator       = cache(new TaskTranslator(this));
		dependencyTranslator = cache(new DependencyTranslator(this));
	}
	
	private <E, MDL> MDLTranslator<E, MDL> cache(MDLTranslator<E, MDL> translator) {
		return new CachingTranslator<>(translator);
	}
		
	public MDLTranslator<Model, MDLFileAST> files() {
		return fileTranslator;
	}

	public MDLTranslator<Model, MDLConfigAST> configs() {
		return configTranslator;
	}
	
	public MDLTranslator<Attribute, MDLAttributeAST> attributes() {
		return attributeTranslator;
	}
	
	public MDLTranslator<Phase, MDLPhaseAST> phases() {
		return phaseTranslator;
	}
	
	public MDLTranslator<TaskType, MDLTaskTypeAST> taskTypes() {
		return taskTypeTranslator;
	}
	
	public MDLTranslator<Task, MDLTaskAST> tasks() {
		return taskTranslator;
	}
	
	public MDLTranslator<Dependency, MDLDependencyAST> dependencies() {
		return dependencyTranslator;
	}
	
}