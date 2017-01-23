package it.unibz.precise.rest.mdl.ast;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

import it.unibz.precise.model.Pitch;
import it.unibz.precise.model.Position;

/**
 * Represents a task in an MDL file.
 * 
 * @author MatthiasP
 *
 */
@JsonIdentityInfo(generator=IntSequenceGenerator.class, property="id", scope=MDLTaskAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLTaskAST {
	
	private MDLTaskTypeAST definition;
	private Pitch pitch;
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

	public Pitch getPitch() {
		return pitch;
	}

	public void setPitch(Pitch pitch) {
		this.pitch = pitch;
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
