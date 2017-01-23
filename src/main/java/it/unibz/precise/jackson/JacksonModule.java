package it.unibz.precise.jackson;

import java.awt.Color;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.unibz.precise.model.ColorMixin;

/**
 * A Jackson module that customizes the JSON representation of Color instances
 * and registers a custom {@link DeserializationProblemHandler}.
 * 
 * @author MatthiasP
 * @see ColorMixin
 * @see CustomDeserializationProblemHandler
 */
@Component
public class JacksonModule extends SimpleModule {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(Color.class, ColorMixin.class);
		context.addDeserializationProblemHandler(new CustomDeserializationProblemHandler());
	}
	
	
}
