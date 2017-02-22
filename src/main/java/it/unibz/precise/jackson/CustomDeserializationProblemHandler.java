package it.unibz.precise.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;
import it.unibz.precise.rest.mdl.ast.MDLConfigAST;
import it.unibz.precise.rest.mdl.ast.MDLCraftAST;
import it.unibz.precise.rest.mdl.ast.MDLDependencyAST;
import it.unibz.precise.rest.mdl.ast.MDLDiagramAST;
import it.unibz.precise.rest.mdl.ast.MDLFileAST;
import it.unibz.precise.rest.mdl.ast.MDLModelAST;
import it.unibz.precise.rest.mdl.ast.MDLOrderSpecificationAST;
import it.unibz.precise.rest.mdl.ast.MDLPhaseAST;
import it.unibz.precise.rest.mdl.ast.MDLScopeAST;
import it.unibz.precise.rest.mdl.ast.MDLTaskAST;
import it.unibz.precise.rest.mdl.ast.MDLActivityAST;

/**
 * Customized Jackson {@link DeserializationProblemHandler}.
 * 
 * A {@code DeserializationProblemHandler} is called whenever Jackson encounters a problem
 * during deserialization and has a chance to recover from the problem, throw a custom exception,
 * or indicate that the problem was not handled.
 * 
 * This is useful when catching the default exception in {@link HttpMessageConversionExceptionHandler}
 * and converting it to a custom message does not work because the exception thrown by Jackson does
 * not include all the data needed for the custom message.
 * 
 * @author MatthiasP
 * @see HttpMessageConversionExceptionHandler
 *
 */
public class CustomDeserializationProblemHandler extends DeserializationProblemHandler {
	
	private final Map<Class<?>, String> mdlClassNames = new HashMap<>();	// Map from MCD-classes to descriptive names
	
	/** Create a {@code CustomDeserializationProblemHandler} */
	public CustomDeserializationProblemHandler() {
		initMdlClassNames();
	}
	
	/** Fill {@code mdlClassNames}. */
	private void initMdlClassNames() {
		mdlClassNames.put(MDLFileAST.class, "MDL-file");
		
		mdlClassNames.put(MDLModelAST.class, "model");
		mdlClassNames.put(MDLConfigAST.class, "configuration");
		mdlClassNames.put(MDLDiagramAST.class, "diagram");
		
		mdlClassNames.put(MDLAttributeAST.class, "attribute");
		mdlClassNames.put(MDLPhaseAST.class, "phase");
		mdlClassNames.put(MDLCraftAST.class, "craft");
		mdlClassNames.put(MDLActivityAST.class, "activity");
		
		mdlClassNames.put(MDLTaskAST.class, "task");
		mdlClassNames.put(MDLDependencyAST.class, "dependency");

		mdlClassNames.put(MDLScopeAST.class, "scope");
		mdlClassNames.put(MDLOrderSpecificationAST.class, "order specification");
	}
	
	/** Returns a name describing the given class, if known. */
	private String getClassName(Class<?> c) {
		return mdlClassNames.get(c);
	}
	
	/** Returns a name describing the type of the given {@link JsonToken}, if known. */
	private String getTokenTypeName(JsonToken t) {
		switch (t) {
		case START_ARRAY:
		case END_ARRAY:
			return "array";
		case START_OBJECT:
		case END_OBJECT:
		case VALUE_EMBEDDED_OBJECT:
			return "object";
		case FIELD_NAME:
			return "field name";
		case VALUE_TRUE:
		case VALUE_FALSE:
			return "boolean";
		case VALUE_NULL:
			return "null";
		case VALUE_NUMBER_FLOAT:
		case VALUE_NUMBER_INT:
			return "number";
		case VALUE_STRING:
			return "string";
		default:
			return null;
		}
	}
	
	@Override
	public Object handleMissingInstantiator(
			DeserializationContext ctxt, Class<?> instClass,
			JsonParser p, String msg) throws IOException
	{
		// Give base implementation a chance to handle the problem
		Object instance = super.handleMissingInstantiator(ctxt, instClass, p, msg);
		if (instance == DeserializationProblemHandler.NOT_HANDLED) {
			// Base implementation could not handle the problem
			// -> check if we know a name of the given class
			String className = getClassName(instClass);
			if (className != null) {
				// Report that an object of the given name was expected
				msg = "Expected " + className;
				// Attempt to obtain the actual type of the value that could not be deserialized
				// to the expected type by looking at the current token.
				String tokenTypeName = getTokenTypeName(p.currentToken());
				if (tokenTypeName != null)
					msg += ", got " + tokenTypeName;
				// Return a corresponding error message.
				// Pass the JsonParser so the problematic location in the file is known.
				throw JsonMappingException.from(p, msg);
			}
		}
		return instance;
	}
	
}