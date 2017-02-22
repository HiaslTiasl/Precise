package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import it.unibz.precise.model.Model;

/**
 * Represents the configuration of an MDL file.
 * 
 * @author MatthiasP
 *
 */
@JsonPropertyOrder({"hoursPerDay", "attributes", "phases", "crafts", "activities"})
public class MDLConfigAST {
	
	public static final MDLConfigAST EMPTY_CONFIG = new MDLConfigAST();
	
	private int hoursPerDay = Model.DEFAULT_HOURS_PER_DAY;
	private List<MDLAttributeAST> attributes;
	private List<MDLPhaseAST> phases;
	private List<MDLCraftAST> crafts;
	private List<MDLActivityAST> activities;
	
	public int getHoursPerDay() {
		return hoursPerDay;
	}

	public void setHoursPerDay(int hoursPerDay) {
		this.hoursPerDay = hoursPerDay;
	}

	public List<MDLCraftAST> getCrafts() {
		return crafts;
	}

	public void setCrafts(List<MDLCraftAST> crafts) {
		this.crafts = crafts;
	}

	public List<MDLAttributeAST> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<MDLAttributeAST> attributes) {
		this.attributes = attributes;
	}
	
	public List<MDLPhaseAST> getPhases() {
		return phases;
	}
	
	public void setPhases(List<MDLPhaseAST> phases) {
		this.phases = phases;
	}

	public List<MDLActivityAST> getActivities() {
		return activities;
	}

	public void setActivities(List<MDLActivityAST> activities) {
		this.activities = activities;
	}
	
}
