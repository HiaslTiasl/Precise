package it.unibz.precise.jackson;

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
import it.unibz.precise.rest.mdl.ast.MDLTaskTypeAST;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

public class CustomDeserializationProblemHandler extends DeserializationProblemHandler {
	
	private final Map<Class<?>, String> mdlClassNames = new HashMap<>();
	
	public CustomDeserializationProblemHandler() {
		initMdlClassNames();
	}
	
	private void initMdlClassNames() {
		mdlClassNames.put(MDLFileAST.class, "MDL-file");
		
		mdlClassNames.put(MDLModelAST.class, "model");
		mdlClassNames.put(MDLConfigAST.class, "configuration");
		mdlClassNames.put(MDLDiagramAST.class, "diagram");
		
		mdlClassNames.put(MDLAttributeAST.class, "attribute");
		mdlClassNames.put(MDLPhaseAST.class, "phase");
		mdlClassNames.put(MDLCraftAST.class, "craft");
		mdlClassNames.put(MDLTaskTypeAST.class, "task definition");
		
		mdlClassNames.put(MDLTaskAST.class, "task");
		mdlClassNames.put(MDLDependencyAST.class, "dependency");

		mdlClassNames.put(MDLScopeAST.class, "scope");
		mdlClassNames.put(MDLOrderSpecificationAST.class, "order specification");
	}
	
	private String getClassName(Class<?> c) {
		return mdlClassNames.get(c);
	}
	
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
		Object instance = super.handleMissingInstantiator(ctxt, instClass, p, msg);
		if (instance == DeserializationProblemHandler.NOT_HANDLED) {
			String className = getClassName(instClass);
			if (className != null) {
				msg = "Expected " + className;
				String tokenTypeName = getTokenTypeName(p.currentToken());
				if (tokenTypeName != null)
					msg += ", got " + tokenTypeName;
				throw JsonMappingException.from(p, msg);
			}
		}
		return instance;
	}
	
}