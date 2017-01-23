package it.unibz.precise.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Represents a position on the diagram plane.
 * 
 * @author MatthiasP
 *
 */
@Embeddable
public class Position {

	@Column(nullable=true)
	private Float x;
	@Column(nullable=true)
	private Float y;
	
	public Float getX() {
		return x;
	}
	
	public void setX(Float x) {
		this.x = x;
	}
	
	public Float getY() {
		return y;
	}
	
	public void setY(Float y) {
		this.y = y;
	}
	
}
