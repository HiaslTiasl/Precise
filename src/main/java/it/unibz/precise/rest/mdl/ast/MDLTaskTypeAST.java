package it.unibz.precise.rest.mdl.ast;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLTaskTypeAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskTypeAST {
	
	private String name;
	private String description;
	private MDLPhaseAST phase;
	private MDLCraftAST craft;
	
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
	
	public MDLPhaseAST getPhase() {
		return phase;
	}
	
	public void setPhase(MDLPhaseAST phase) {
		this.phase = phase;
	}

	public MDLCraftAST getCraft() {
		return craft;
	}

	public void setCraft(MDLCraftAST craft) {
		this.craft = craft;
	}

}
