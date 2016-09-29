package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.util.Util;

class FileTranslator extends AbstractMDLTranslator<Model, MDLFileAST> {
	
	FileTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Model model, MDLFileAST mdlFile) {
		mdlFile.setModel(context().models().toMDL(model));
		mdlFile.setConfig(context().configs().toMDL(model));
		mdlFile.setTasks(Util.mapToList(model.getTasks(), context().tasks()::toMDL));
		mdlFile.setDependencies(Util.mapToList(model.getDependencies(), context().dependencies()::toMDL));
	}
	
	@Override
	public void updateEntity(MDLFileAST mdlFile, Model model) {
		//model.setName(name);
		context().models().updateEntity(mdlFile.getModel(), model);
		context().configs().updateEntity(mdlFile.getConfig(), model);
		mdlFile.getTasks().stream()
			.map(context().tasks()::toEntity)
			.forEach(model::addTask);
		mdlFile.getDependencies().stream()
			.map(context().dependencies()::toEntity)
			.forEach(model::addDependency);
	}

	@Override
	public Model createEntity(MDLFileAST mdl) {
		return context().models().toEntity(mdl.getModel());
	}

	@Override
	public MDLFileAST createMDL(Model entity) {
		return new MDLFileAST();
	}
	
}
