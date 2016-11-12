package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLModelAST;

class ModelTranslator extends AbstractMDLTranslator<Model, MDLModelAST> {
	
	ModelTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public Model createEntity(MDLModelAST mdl) {
		return new Model();
	}

	@Override
	public MDLModelAST createMDL(Model entity) {
		return new MDLModelAST();
	}

	@Override
	protected void updateEntityImpl(MDLModelAST mdlModel, Model model) {
		model.setDescription(mdlModel.getDescription());
	}

	@Override
	protected void updateMDLImpl(Model model, MDLModelAST mdlModel) {
		mdlModel.setDescription(model.getDescription());
	}

}
