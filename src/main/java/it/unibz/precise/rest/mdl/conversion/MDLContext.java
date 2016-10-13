package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLDependencyAST;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.ast.MDLModelAST;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.precise.rest.mdl.ast.MDLScopeAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskTypeAST;

public class MDLContext {
	
	private final MDLTranslator<Model, MDLFileAST> fileTranslator;
	private final MDLTranslator<Model, MDLModelAST> modelTranslator;
	private final MDLTranslator<Model, MDLConfigAST> configTranslator;
	private final MDLTranslator<Model, MDLDiagramAST> diagramTranslator;
	private final MDLTranslator<Attribute, MDLAttributeAST> attributeTranslator;
	private final MDLTranslator<Phase, MDLPhaseAST> phaseTranslator;
	private final MDLTranslator<TaskType, MDLTaskTypeAST> taskTypeTranslator;
	private final MDLTranslator<Task, MDLTaskAST> taskTranslator;
	private final MDLTranslator<Dependency, MDLDependencyAST> dependencyTranslator;
	private final MDLTranslator<Scope, MDLScopeAST> scopeTranslator;
	
	public MDLContext() {
		fileTranslator       = cache(new FileTranslator(this));
		modelTranslator      = cache(new ModelTranslator(this));
		configTranslator     = cache(new ConfigTranslator(this));
		diagramTranslator    = cache(new DiagramTranslator(this));
		attributeTranslator  = cache(new AttributeTranslator(this));
		phaseTranslator      = cache(new PhaseTranslator(this));
		taskTypeTranslator   = cache(new TaskTypeTranslator(this));
		taskTranslator       = cache(new TaskTranslator(this));
		dependencyTranslator = cache(new DependencyTranslator(this));
		scopeTranslator      = cache(new ScopeTranslator(this));
	}
	
	private <E, MDL> MDLTranslator<E, MDL> cache(MDLTranslator<E, MDL> translator) {
		return new CachingTranslator<>(translator);
	}
		
	public MDLTranslator<Model, MDLFileAST> files() {
		return fileTranslator;
	}
	
	public MDLTranslator<Model, MDLModelAST> models() {
		return modelTranslator;
	}

	public MDLTranslator<Model, MDLConfigAST> configs() {
		return configTranslator;
	}
	
	public MDLTranslator<Model, MDLDiagramAST> diagrams() {
		return diagramTranslator;
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
	
	public MDLTranslator<Scope, MDLScopeAST> scopes() {
		return scopeTranslator;
	}
	
}
