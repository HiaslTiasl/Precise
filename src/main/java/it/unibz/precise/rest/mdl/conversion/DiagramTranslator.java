package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.util.Util;

/**
 * {@link MDLTranslator} for the diagram part.
 * 
 * @author MatthiasP
 *
 */
class DiagramTranslator extends AbstractMDLTranslator<Model, MDLDiagramAST> {
	
	DiagramTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Model model, MDLDiagramAST mdlDiagram) {
		mdlDiagram.setTasks(Util.mapToList(model.getTasks(), context().tasks()::toMDL));
		mdlDiagram.setDependencies(Util.mapToList(model.getDependencies(), context().dependencies()::toMDL));
	}
	
	@Override
	protected void updateEntityImpl(MDLDiagramAST mdlDiagram, Model model) {
		model.setTasks(Util.mapToList(mdlDiagram.getTasks(), context().tasks()::toEntity));
		model.setDependencies(Util.mapToList(mdlDiagram.getDependencies(), context().dependencies()::toEntity));
	}

	@Override
	public Model createEntity() {
		return new Model();
	}

	@Override
	public MDLDiagramAST createMDL() {
		return new MDLDiagramAST();
	}

}
