package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.util.Util;

class ConfigTranslator extends AbstractMDLTranslator<Model, MDLConfigAST> {
	
	ConfigTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Model model, MDLConfigAST mdlAttribute) {
		mdlAttribute.setAttributes(Util.mapToList(model.getAttributes(), context().attributes()::toMDL));
		mdlAttribute.setPhases(Util.mapToList(model.getPhases(), context().phases()::toMDL));
		mdlAttribute.setTaskTypes(Util.mapToList(model.getTaskTypes(), context().taskTypes()::toMDL));
	}
	
	@Override
	public void updateEntity(MDLConfigAST mdlAttribute, Model model) {
		model.setAttributes(Util.mapToList(mdlAttribute.getAttributes(), context().attributes()::toEntity));
		model.setPhases(Util.mapToList(mdlAttribute.getPhases(), context().phases()::toEntity));
		model.setTaskTypes(Util.mapToList(mdlAttribute.getTaskTypes(), context().taskTypes()::toEntity));
	}

	@Override
	public Model createEntity(MDLConfigAST mdl) {
		return new Model();
	}

	@Override
	public MDLConfigAST createMDL(Model entity) {
		return new MDLConfigAST();
	}

}
