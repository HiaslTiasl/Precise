package it.unibz.precise.rest.mdl.ast;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import it.unibz.precise.model.TaskType;

@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLTaskTypeAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskTypeAST {
	
	@JsonIgnore
	private TaskType taskType;

	private String name;
	private String description;
	private String craft;
	private MDLPhaseAST phase;
	
	
	public MDLTaskTypeAST() {
	}

	public MDLTaskTypeAST(MDLFileContext context, TaskType taskType) {
		this.taskType = taskType;
		name = taskType.getName();
		description = taskType.getDescription();
		craft = taskType.getCraft();
		phase = context.translate(taskType.getPhase());
	}
	
	public TaskType toTaskType() {
		if (taskType == null) {
			taskType = new TaskType();
			taskType.setName(name);
			taskType.setDescription(description);
			taskType.setCraft(craft);
			taskType.setPhase(phase.toPhase());
		}
		return taskType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCraft() {
		return craft;
	}

	public void setCraft(String craft) {
		this.craft = craft;
	}

	public MDLPhaseAST getPhase() {
		return phase;
	}

	public void setPhase(MDLPhaseAST phase) {
		this.phase = phase;
	}
	
}
