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
		mdlTaskType.setCraft(taskType.getCraft());
		mdlTaskType.setCraftShort(taskType.getCraftShort());
	}
	
	@Override
	public void updateEntity(MDLTaskTypeAST mdlTaskType, TaskType taskType) {
		taskType.setName(mdlTaskType.getName());
		taskType.setDescription(mdlTaskType.getDescription());
		taskType.setPhase(context().phases().toEntity(mdlTaskType.getPhase()));
		taskType.setCraft(mdlTaskType.getCraft());
		taskType.setCraftShort(mdlTaskType.getCraftShort());
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
