package it.unibz.precise.jackson;

import java.awt.Color;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.module.SimpleModule;

import it.unibz.precise.model.ColorMixin;

@Component
public class JacksonModule extends SimpleModule {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(Color.class, ColorMixin.class);
	}
	
	
}
