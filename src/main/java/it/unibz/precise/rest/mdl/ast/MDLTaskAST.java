package it.unibz.precise.rest.mdl.ast;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

import it.unibz.precise.model.OrderType;
import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;

@JsonIdentityInfo(generator=IntSequenceGenerator.class, property="id", scope=MDLTaskAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskAST {
	
	@JsonIgnore
	private Task task;
	
	private MDLTaskTypeAST type;
	private float numberOfWorkersNeeded;
	private float numberOfUnitsPerDay;
	private boolean globalExclusiveness;
	private List<MDLAttributeAST> exclusiveness;
	private Map<String, OrderType> order;
	private Position position;
	private List<Map<String, String>> locations;
	
	public MDLTaskTypeAST getType() {
		return type;
	}

	public void setType(MDLTaskTypeAST type) {
		this.type = type;
	}

	public float getNumberOfWorkersNeeded() {
		return numberOfWorkersNeeded;
	}

	public void setNumberOfWorkersNeeded(float numberOfWorkersNeeded) {
		this.numberOfWorkersNeeded = numberOfWorkersNeeded;
	}

	public float getNumberOfUnitsPerDay() {
		return numberOfUnitsPerDay;
	}

	public void setNumberOfUnitsPerDay(float numberOfUnitsPerDay) {
		this.numberOfUnitsPerDay = numberOfUnitsPerDay;
	}

	public boolean isGlobalExclusiveness() {
		return globalExclusiveness;
	}

	public void setGlobalExclusiveness(boolean globalExclusiveness) {
		this.globalExclusiveness = globalExclusiveness;
	}

	public List<MDLAttributeAST> getExclusiveness() {
		return exclusiveness;
	}

	public void setExclusiveness(List<MDLAttributeAST> exclusiveness) {
		this.exclusiveness = exclusiveness;
	}

	public Map<String, OrderType> getOrder() {
		return order;
	}

	public void setOrder(Map<String, OrderType> order) {
		this.order = order;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<Map<String, String>> getLocations() {
		return locations;
	}

	public void setLocations(List<Map<String, String>> locations) {
		this.locations = locations;
	}

}
