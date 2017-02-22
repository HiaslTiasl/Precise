package it.unibz.precise.model;

import java.util.HashMap;
import java.util.Map;

public class UniqueKeys {

	private static final Map<String, String[]> mappings = createMappings();
	
	public static String[] lookup(String constraintName) {
		return mappings.get(constraintName);
	}
	
	private static void map(Map<String, String[]> mappings, String constraintName, String... fields) {
		mappings.put(constraintName, fields);
	}
	
	private static Map<String, String[]> createMappings() {
		Map<String, String[]> mappings = new HashMap<>();
		
		map(mappings, Model.UC_NAME, "name");
		
		// Attribute
		map(mappings, Attribute.UC_NAME, "name");
		
		// Phase
		map(mappings, Phase.UC_NAME, "name");
		
		// AttributeHierarchyLevel
		map(mappings, AttributeHierarchyLevel.UC_PHASE_ATTRIBUTE, "phase", "attribute");
		map(mappings, AttributeHierarchyLevel.UC_PHASE_POSITION, "phase", "position");
		
		// AttributeHierarchyNode
		map(mappings, AttributeHierarchyNode.UC_PARENT_VALUE, "parent", "value");
		//map(mappings, AttributeHierarchyNode.UC_LEVEL_VALUE, "level", "value");
		
		// Craft
		map(mappings, Craft.UC_NAME, "name");
		map(mappings, Craft.UC_SHORTNAME, "shortName");
		
		// Activity
		map(mappings, Activity.UC_NAME, "name");
		map(mappings, Activity.UC_SHORTNAME, "shortName");

		// Dependency
		map(mappings, Dependency.UC_SOURCE_TARGET, "source", "target");
		
		return mappings;
		
	}
	
}
