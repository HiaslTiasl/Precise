package it.unibz.precise.model;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ColorMixin extends Color {
	
	private static final long serialVersionUID = 1L;
	
	ColorMixin(@JsonProperty("red") int red, @JsonProperty("green") int green, @JsonProperty("blue") int blue) {
		super(red, green, blue);
	}
	
	@Override @JsonProperty("red") public abstract int getRed();
	@Override @JsonProperty("green") public abstract int getGreen();
	@Override @JsonProperty("blue") public abstract int getBlue();
}