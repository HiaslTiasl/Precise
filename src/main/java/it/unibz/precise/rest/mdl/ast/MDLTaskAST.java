package it.unibz.precise.rest.mdl.ast;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

import it.unibz.precise.model.Position;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.Task.DurationType;

@JsonIdentityInfo(generator=IntSequenceGenerator.class, property="id", scope=MDLTaskAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskAST {
	
	private MDLTaskTypeAST definition;
	private DurationType durationType;
	private Integer totalQuantity;
	private Float quantityPerDay;
	private Integer crewSize;
	private Integer crewCount = Task.DEFAULT_CREW_COUNT;
	private Integer durationDays;
	private MDLScopeAST exclusiveness;
	private List<MDLOrderSpecificationAST> order;
	private Position position;
	private List<Map<String, String>> locations;
	
	public MDLTaskTypeAST getDefinition() {
		return definition;
	}

	public void setDefinition(MDLTaskTypeAST definition) {
		this.definition = definition;
	}

	public DurationType getDurationType() {
		return durationType;
	}

	public void setDurationType(DurationType durationType) {
		this.durationType = durationType;
	}

	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Float getQuantityPerDay() {
		return quantityPerDay;
	}

	public void setQuantityPerDay(Float quantityPerDay) {
		this.quantityPerDay = quantityPerDay;
	}

	public Integer getCrewSize() {
		return crewSize;
	}

	public void setCrewSize(Integer crewSize) {
		this.crewSize = crewSize;
	}

	public Integer getCrewCount() {
		return crewCount;
	}

	public void setCrewCount(Integer crewCount) {
		this.crewCount = crewCount;
	}

	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}

	public MDLScopeAST getExclusiveness() {
		return exclusiveness;
	}

	public void setExclusiveness(MDLScopeAST exclusiveness) {
		this.exclusiveness = exclusiveness;
	}

	public List<MDLOrderSpecificationAST> getOrder() {
		return order;
	}

	public void setOrder(List<MDLOrderSpecificationAST> order) {
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
