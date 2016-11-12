package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Position;
import it.unibz.precise.rest.mdl.ast.MDLDependencyAST;

import java.util.ArrayList;
import java.util.List;

class DependencyTranslator extends AbstractMDLTranslator<Dependency, MDLDependencyAST> {
	
	DependencyTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Dependency dependency, MDLDependencyAST mdlDependency) {
		mdlDependency.setAlternate(dependency.isAlternate());
		mdlDependency.setChain(dependency.isChain());
		mdlDependency.setSource(context().tasks().toMDL(dependency.getSource()));
		mdlDependency.setTarget(context().tasks().toMDL(dependency.getTarget()));
		mdlDependency.setSourceVertex(dependency.getSourceVertex());
		mdlDependency.setTargetVertex(dependency.getTargetVertex());
		mdlDependency.setVertices(dependency.getVertices());
		mdlDependency.setLabelPosition(dependency.getLabelPosition());
		mdlDependency.setScope(context().scopes().toMDL(dependency.getScope()));
	}

	@Override
	protected void updateEntityImpl(MDLDependencyAST mdlDependency, Dependency dependency) {
		dependency.setAlternate(mdlDependency.isAlternate());
		dependency.setChain(mdlDependency.isChain());
		dependency.setSource(context().tasks().toEntity(mdlDependency.getSource()));
		dependency.setTarget(context().tasks().toEntity(mdlDependency.getTarget()));
		dependency.setSourceVertex(mdlDependency.getSourceVertex());
		dependency.setTargetVertex(mdlDependency.getTargetVertex());
		// N.B. we defensively use a new collection to prevent errors when converting forth and back,
		// since embedded collections cannot be shared.
		List<Position> vertices = mdlDependency.getVertices();
		dependency.setVertices(vertices == null ? null : new ArrayList<>(vertices));
		dependency.setLabelPosition(mdlDependency.getLabelPosition());
		dependency.setScope(context().scopes().toEntity(mdlDependency.getScope()));
	}

	@Override
	public Dependency createEntity(MDLDependencyAST mdl) {
		return new Dependency();
	}

	@Override
	public MDLDependencyAST createMDL(Dependency entity) {
		return new MDLDependencyAST();
	}
	
}
