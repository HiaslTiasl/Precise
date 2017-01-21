package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Model;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.util.Util;

class ConfigTranslator extends AbstractMDLTranslator<Model, MDLConfigAST> {
	
	ConfigTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Model model, MDLConfigAST mdlConfig) {
		mdlConfig.setHoursPerDay(model.getHoursPerDay());
		mdlConfig.setAttributes(Util.mapToList(model.getAttributes(), context().attributes()::toMDL));
		mdlConfig.setPhases(Util.mapToList(model.getPhases(), context().phases()::toMDL));
		mdlConfig.setCrafts(Util.mapToList(model.getCrafts(), context().crafts()::toMDL));
		mdlConfig.setTaskDefinitions(Util.mapToList(model.getTaskTypes(), context().taskTypes()::toMDL));
	}
	
	@Override
	protected void updateEntityImpl(MDLConfigAST mdlConfig, Model model) {
		model.setHoursPerDay(mdlConfig.getHoursPerDay());
		model.setAttributes(Util.mapToList(mdlConfig.getAttributes(), context().attributes()::toEntity));
		model.setPhases(Util.mapToList(mdlConfig.getPhases(), context().phases()::toEntity));
		model.setCrafts(Util.mapToList(mdlConfig.getCrafts(), context().crafts()::toEntity));
		model.setTaskTypes(Util.mapToList(mdlConfig.getTaskDefinitions(), context().taskTypes()::toEntity));
	}

	@Override
	public Model createEntity() {
		return new Model();
	}

	@Override
	public MDLConfigAST createMDL() {
		return new MDLConfigAST();
	}

}
