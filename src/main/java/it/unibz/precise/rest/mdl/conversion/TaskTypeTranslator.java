package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.TaskType;
import it.unibz.precise.rest.mdl.ast.MDLTaskTypeAST;

class TaskTypeTranslator extends AbstractMDLTranslator<TaskType, MDLTaskTypeAST> {
	
	TaskTypeTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(TaskType taskType, MDLTaskTypeAST mdlTaskType) {
		mdlTaskType.setName(taskType.getName());
		mdlTaskType.setDescription(taskType.getDescription());
		mdlTaskType.setPhase(context().phases().toMDL(taskType.getPhase()));
		mdlTaskType.setCraft(context().crafts().toMDL(taskType.getCraft()));
	}
	
	@Override
	public void updateEntity(MDLTaskTypeAST mdlTaskType, TaskType taskType) {
		taskType.setName(mdlTaskType.getName());
		taskType.setDescription(mdlTaskType.getDescription());
		taskType.setPhase(context().phases().toEntity(mdlTaskType.getPhase()));
		taskType.setCraft(context().crafts().toEntity(mdlTaskType.getCraft()));
	}

	@Override
	public TaskType createEntity(MDLTaskTypeAST mdl) {
		return new TaskType();
	}

	@Override
	public MDLTaskTypeAST createMDL(TaskType entity) {
		return new MDLTaskTypeAST();
	}

}
