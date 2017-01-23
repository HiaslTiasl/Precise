package it.unibz.precise.rest.mdl.ast;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import it.unibz.precise.model.ShortNameProvider;

/**
 * Represents a craft in an MDL file.
 * 
 * @author MatthiasP
 *
 */
@JsonIdentityInfo(generator=PropertyGenerator.class, property="name", scope=MDLCraftAST.class)
@JsonIdentityReference(alwaysAsId=false)
public class MDLCraftAST implements ShortNameProvider {

	private String name;
	private String shortName;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
}
