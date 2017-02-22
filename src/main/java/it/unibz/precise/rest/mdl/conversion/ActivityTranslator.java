package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Activity;
import it.unibz.precise.rest.mdl.ast.MDLActivityAST;

/**
 * {@link MDLTranslator} for activities.
 * 
 * @author MatthiasP
 *
 */
class ActivityTranslator extends AbstractMDLTranslator<Activity, MDLActivityAST> {
	
	ActivityTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Activity activity, MDLActivityAST mdlActivity) {
		mdlActivity.setName(activity.getName());
		mdlActivity.setShortName(activity.getShortName());
		mdlActivity.setDescription(activity.getDescription());
		mdlActivity.setUnitOfMeasure(activity.getUnitOfMeasure());
		mdlActivity.setPhase(context().phases().toMDL(activity.getPhase()));
		mdlActivity.setCraft(context().crafts().toMDL(activity.getCraft()));
	}
	
	@Override
	protected void updateEntityImpl(MDLActivityAST mdlActivity, Activity activity) {
		activity.setName(mdlActivity.getName());
		activity.setShortName(mdlActivity.getShortName());
		activity.setDescription(mdlActivity.getDescription());
		activity.setUnitOfMeasure(mdlActivity.getUnitOfMeasure());		
		activity.setPhase(context().phases().toEntity(mdlActivity.getPhase()));
		activity.setCraft(context().crafts().toEntity(mdlActivity.getCraft()));
	}

	@Override
	public Activity createEntity() {
		return new Activity();
	}

	@Override
	public MDLActivityAST createMDL() {
		return new MDLActivityAST();
	}

}
