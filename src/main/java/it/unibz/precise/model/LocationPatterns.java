package it.unibz.precise.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.unibz.util.Util;

/**
 * Static methods for working with "location patterns", i.e. locations specified
 * as a map from attribute names to {@link PatternEntry}.
 * 
 * @author MatthiasP
 *
 */
public class LocationPatterns {
	
	/** Converts the given {@link Location} to a pattern according to the given {@link Phase}. */
	public static Map<String, PatternEntry> locationToPattern(Location location, Phase phase) {
		return phase == null ? null : nodeToPattern(location.getNode(), phase.getAttributeHierarchyLevels());
	}

	/** Converts the given {@link Location} to a pattern according to the given {@link AttributeHierarchyLevel}s. */
	public static Map<String, PatternEntry> locationToPattern(Location location, List<AttributeHierarchyLevel> levels) {
		return nodeToPattern(location.getNode(), levels);
	}
	
	/** Converts the given {@link AttributeHierarchyNode} to a pattern according to the given {@link AttributeHierarchyLevel}s. */
	public static Map<String, PatternEntry> nodeToPattern(AttributeHierarchyNode node, List<AttributeHierarchyLevel> levels) {
		return checkPattern(AttributeHierarchyNode.toPattern(node, levels), levels);
	}
	
	/** Reorder the given values according to their appearance in the range of the given {@link Attribute}. */
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
	
	/**
	 * Checks the given pattern in terms of allowed values.
	 * Returns null if {@code phase} is null.
	 * @see #checkPattern(Map, List)
	 */
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
	{
		if (pattern == null)
			pattern = new LinkedHashMap<String, PatternEntry>();
		int levelCount = Util.size(levels);
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
	
	/**
	 * Converts a pattern to an {@link AttributeHierarchyNode}.
	 * @throws InvalidLocationException if {@code pattern} is invalid according
	 *         to the hierarchy represented by {@code levels}.
	 * @see #patternToNode(Task, Map, List, boolean)
	 */
	public static AttributeHierarchyNode patternToNode(
		Task task,
		Map<String, PatternEntry> pattern, List<AttributeHierarchyLevel> levels)
		throws InvalidLocationException
	{
		return patternToNode(task, pattern, levels, true);
	}

	/**
	 * Converts a pattern to an {@link AttributeHierarchyNode}.
	 * @param task The task containing the location pattern
	 * @param pattern the pattern to be converted
	 * @param levels The levels of the CA hierarchy of the task's phase.
	 * @param strict Used to control behavior if the pattern does not match
	 *        the hierarchy. If set to true, an exception will be thrown,
	 *        otherwise the most specific {@code AttributeHierarchyNode}
	 *        found so far will be returned.
	 * @return The {@code AttributeHierarchyNode} corresponding to the given pattern.
	 * @throws InvalidLocationException if @{@code strict} is @{@code true}
	 *         and {@code pattern} is invalid according to the hierarchy
	 *         represented by {@code levels}.
	 */
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
		
		boolean encounteredWildcard = false;	// Did we encounter a wildcard previously?
		
		if (levelCount > 0) {
			Map<String, AttributeHierarchyNode> subTree = levels.get(0).getNodes();		// Subtree for current node 
			int len = Math.min(entryCount, levelCount);
			for (int i = 0; i < len; i++) {
				AttributeHierarchyLevel level = levels.get(i);
				String attrString = level.getAttribute().getName();
				PatternEntry entry = pattern.get(attrString);
				
				if (entry == null || !entry.hasValue()) {
					// No value for this attribute --> wildcard encountered, subTree not defined
					encounteredWildcard = true;
					subTree = null;
				}
				else if (encounteredWildcard) {
					// A wildcard was encountered previously, but current entry has actual value
					if (strict)
						throw new MissingIntermediateEntryException(task, pattern, attrString);
					else
						break;
				}
				else if (subTree != null) {
					// Walk down hierarchy on path specified by pattern
					String value = pattern.get(attrString).getValue();
					AttributeHierarchyNode node = subTree.get(value);
							
					if (node != null) {
						parent = node;
						subTree = node.getChildren();
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
