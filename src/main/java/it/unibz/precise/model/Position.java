package it.unibz.precise.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Position {

	@Column(nullable=true)
	private float x;
	@Column(nullable=true)
	private float y;
	
	public float getX() {
		return x;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
}
