package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.rest.mdl.ast.MDLDependencyAST;
import it.unibz.util.Util;

class DependencyTranslator extends AbstractMDLTranslator<Dependency, MDLDependencyAST> {
	
	DependencyTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Dependency dependency, MDLDependencyAST mdlDependency) {
		mdlDependency.setAlternate(dependency.isAlternate());
		mdlDependency.setChain(dependency.isChain());
		mdlDependency.setSource(context().tasks().toMDL(dependency.getSource()));
		mdlDependency.setTarget(context().tasks().toMDL(dependency.getTarget()));
		mdlDependency.setSourceVertex(dependency.getSourceVertex());
		mdlDependency.setTargetVertex(dependency.getTargetVertex());
		mdlDependency.setVertices(dependency.getVertices());
		mdlDependency.setGlobalScope(dependency.isGlobalScope());
		mdlDependency.setScope(Util.mapToList(dependency.getScope(), context().attributes()::toMDL));
	}
	
	@Override
	public void updateEntity(MDLDependencyAST mdlDependency, Dependency dependency) {
		dependency.setAlternate(mdlDependency.isAlternate());
		dependency.setChain(mdlDependency.isChain());
		dependency.setSource(context().tasks().toEntity(mdlDependency.getSource()));
		dependency.setTarget(context().tasks().toEntity(mdlDependency.getTarget()));
		dependency.setSourceVertex(mdlDependency.getSourceVertex());
		dependency.setTargetVertex(mdlDependency.getTargetVertex());
		dependency.setVertices(mdlDependency.getVertices());
		dependency.setGlobalScope(mdlDependency.isGlobalScope());
		dependency.setScope(Util.mapToList(mdlDependency.getScope(), context().attributes()::toEntity));
	}

	@Override
	public Dependency createEntity() {
		return new Dependency();
	}

	@Override
	public MDLDependencyAST createMDL() {
		return new MDLDependencyAST();
	}
	
}
