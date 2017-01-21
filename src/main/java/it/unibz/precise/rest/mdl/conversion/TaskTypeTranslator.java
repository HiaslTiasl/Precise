package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.ast.MDLTaskTypeAST;

class TaskTypeTranslator extends AbstractMDLTranslator<TaskType, MDLTaskTypeAST> {
	
	TaskTypeTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(TaskType taskType, MDLTaskTypeAST mdlTaskType) {
		mdlTaskType.setName(taskType.getName());
		mdlTaskType.setShortName(taskType.getShortName());
		mdlTaskType.setDescription(taskType.getDescription());
		mdlTaskType.setUnitOfMeasure(taskType.getUnitOfMeasure());
		mdlTaskType.setPhase(context().phases().toMDL(taskType.getPhase()));
		mdlTaskType.setCraft(context().crafts().toMDL(taskType.getCraft()));
	}
	
	@Override
	protected void updateEntityImpl(MDLTaskTypeAST mdlTaskType, TaskType taskType) {
		taskType.setName(mdlTaskType.getName());
		taskType.setShortName(mdlTaskType.getShortName());
		taskType.setDescription(mdlTaskType.getDescription());
		taskType.setUnitOfMeasure(mdlTaskType.getUnitOfMeasure());		
		taskType.setPhase(context().phases().toEntity(mdlTaskType.getPhase()));
		taskType.setCraft(context().crafts().toEntity(mdlTaskType.getCraft()));
	}

	@Override
	public TaskType createEntity() {
		return new TaskType();
	}

	@Override
	public MDLTaskTypeAST createMDL() {
		return new MDLTaskTypeAST();
	}

}
