package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocationPatterns {
	
	public static Map<String, PatternEntry> locationToPattern(Location location, Phase phase) {
		return phase == null ? null : nodeToPattern(location.getNode(), phase.getAttributeHierarchyLevels());
	}

	public static Map<String, PatternEntry> locationToPattern(Location location, List<AttributeHierarchyLevel> levels) {
		return nodeToPattern(location.getNode(), levels);
	}
	
	public static Map<String, PatternEntry> nodeToPattern(AttributeHierarchyNode node, List<AttributeHierarchyLevel> levels) {
		return checkPattern(AttributeHierarchyNode.toPattern(node, levels), levels);
	}
	
	/** Reorder the given values according to their appearance in */
	private static List<String> ordered(Attribute attribute, Collection<String> values) {
		return attribute.getRange().stream()
			.filter(values::contains)
			.collect(Collectors.toList());
	}
	
	/**
	 * Set allowed values for the given attribute in the given pattern.
	 * If the current value in the corresponding entry is not contained in the allowed values,
	 * it is set to wildcard.
	 * If the corresponding entry is null, a new one is created with a wildcard value.
	 */
	private static PatternEntry setAllowedValuesToNodes(Map<String, PatternEntry> pattern, Attribute attribute, List<String> values) {
		values.add(0, PatternEntry.WILDCARD_VALUE);
		String attrName = attribute.getName();
		PatternEntry entry = pattern.get(attrName);
		boolean create = entry == null;
		// A missing entry is equivalent to one with a wildcard
		if (create) {
			entry = new PatternEntry(attrName);
			pattern.put(attrName, entry);
		}
		entry.setAllowedValues(values);
		if (!create)
			entry.checkValue();
		return entry;
	}
	
	public static Map<String, PatternEntry> checkPattern(
		Map<String, PatternEntry> pattern, Phase phase) throws InvalidLocationException
	{
		return phase == null ? null : checkPattern(pattern, phase.getAttributeHierarchyLevels());
	}
	
	/**
	 * Checks the given pattern in terms of allowed values.
	 * A PatternEntry with a value that is not contained in its allowed values is set to wildcard.
	 * Any missing PatternEntries are initialized as wildcards.
	 * If pattern is null, a global pattern is returned.
	 */
	public static Map<String, PatternEntry> checkPattern(
		Map<String, PatternEntry> pattern, List<AttributeHierarchyLevel> levels)
		throws InvalidLocationException
	{
		if (pattern == null)
			pattern = new LinkedHashMap<String, PatternEntry>();
		int levelCount = levels.size();
		if (levelCount > 0) {
			Map<String, AttributeHierarchyNode> tree = levels.get(0).getNodes();
			for (int i = 0; i < levelCount; i++) {
				AttributeHierarchyLevel level = levels.get(i);
				Attribute attribute = level.getAttribute();
				List<String> allowedValues = tree == null ? new ArrayList<>() : ordered(attribute, tree.keySet());
				PatternEntry entry = setAllowedValuesToNodes(pattern, attribute, allowedValues);
				String value = entry.getValue();
				if (tree != null) {
					AttributeHierarchyNode node = tree.get(value);
					tree = node == null ? null : node.getChildren();
				}
			}
		}
		return pattern;
	}
	
	public static AttributeHierarchyNode patternToNode(
		Task task,
		Map<String, PatternEntry> pattern, List<AttributeHierarchyLevel> levels)
		throws InvalidLocationException
	{
		return patternToNode(task, pattern, levels, true);
	}

	public static AttributeHierarchyNode patternToNode(
		Task task,
		Map<String, PatternEntry> pattern,
		List<AttributeHierarchyLevel> levels, boolean strict)
		throws InvalidLocationException
	{
		
		int entryCount = pattern.size();
		int levelCount = levels.size();
		if (strict && entryCount > levelCount)
			throw new LocationHierarchyMismatchException(task, pattern);
		
		AttributeHierarchyNode parent = null;
		
		boolean encounteredWildcard = false;
		
		if (levelCount > 0) {
			Map<String, AttributeHierarchyNode> tree = levels.get(0).getNodes();
			int len = Math.min(entryCount, levelCount);
			for (int i = 0; i < len; i++) {
				AttributeHierarchyLevel level = levels.get(i);
				String attrString = level.getAttribute().getName();
				PatternEntry entry = pattern.get(attrString);
				
				if (entry == null || !entry.hasValue()) {
					encounteredWildcard = true;
					tree = null;
				}
				else if (encounteredWildcard) {
					if (strict)
						throw new MissingIntermediateEntryException(task, pattern, attrString);
					else
						break;
				}
				else if (tree != null) {
					String value = pattern.get(attrString).getValue();
					
					AttributeHierarchyNode node = tree.get(value);
							
					if (node != null) {
						parent = node;
						tree = node.getChildren();
					}
					else if (strict)
						throw new NonExistingLocationException(task, pattern, nodeToPattern(parent, levels), attrString, value);
					else
						break;
				}
			}
		}
			
		return parent;
	}
	
}
