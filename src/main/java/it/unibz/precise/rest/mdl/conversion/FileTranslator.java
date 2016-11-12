package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;

class FileTranslator extends AbstractMDLTranslator<Model, MDLFileAST> {
	
	FileTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Model model, MDLFileAST mdlFile) {
		mdlFile.setModel(context().models().toMDL(model));
		mdlFile.setConfiguration(context().configs().toMDL(model));
		mdlFile.setDiagram(context().diagrams().toMDL(model));
	}
	
	@Override
	protected void updateEntityImpl(MDLFileAST mdlFile, Model model) {
		context().models().updateEntity(mdlFile.getModel(), model);
		context().configs().updateEntity(mdlFile.getConfiguration(), model);
		context().diagrams().updateEntity(mdlFile.getDiagram(), model);
	}

	@Override
	public Model createEntity(MDLFileAST mdl) {
		return new Model();
	}

	@Override
	public MDLFileAST createMDL(Model entity) {
		return new MDLFileAST();
	}
	
}
