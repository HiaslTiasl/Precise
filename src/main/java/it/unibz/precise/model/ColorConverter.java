package it.unibz.precise.model;

import java.awt.Color;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ColorConverter implements AttributeConverter<Color, Integer>{

	@Override
	public Integer convertToDatabaseColumn(Color attribute) {
		return attribute == null ? null : attribute.getRGB();
	}

	@Override
	public Color convertToEntityAttribute(Integer dbData) {
		return dbData == null ? null : new Color(dbData);
	}

}
