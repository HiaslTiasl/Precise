package it.unibz.precise.model;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class ColorMixin extends Color {
	
	private static final long serialVersionUID = 1L;
	
	ColorMixin(@JsonProperty("r") int red, @JsonProperty("g") int green, @JsonProperty("b") int blue) {
		super(red, green, blue);
	}
	
	@Override @JsonProperty("r") public abstract int getRed();
	@Override @JsonProperty("g") public abstract int getGreen();
	@Override @JsonProperty("b") public abstract int getBlue();
}