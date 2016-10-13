package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.util.Util;

class DiagramTranslator extends AbstractMDLTranslator<Model, MDLDiagramAST> {
	
	DiagramTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Model model, MDLDiagramAST mdlDiagram) {
		mdlDiagram.setTasks(Util.mapToList(model.getTasks(), context().tasks()::toMDL));
		mdlDiagram.setDependencies(Util.mapToList(model.getDependencies(), context().dependencies()::toMDL));
	}
	
	@Override
	public void updateEntity(MDLDiagramAST mdlDiagram, Model model) {
		mdlDiagram.getTasks().stream()
			.map(context().tasks()::toEntity)
			.forEach(model::addTask);
		mdlDiagram.getDependencies().stream()
			.map(context().dependencies()::toEntity)
			.forEach(model::addDependency);
	}

	@Override
	public Model createEntity(MDLDiagramAST mdl) {
		return new Model();
	}

	@Override
	public MDLDiagramAST createMDL(Model entity) {
		return new MDLDiagramAST();
	}

}
