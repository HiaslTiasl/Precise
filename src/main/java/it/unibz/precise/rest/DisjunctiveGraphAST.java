package it.unibz.precise.rest;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.unibz.precise.check.ModelToGraphTranslator;
import it.unibz.precise.check.TaskUnitNode;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.PatternEntry;

public class DisjunctiveGraphAST {

	private List<String> nodes;
	private Map<String, List<String>> arcs;
	private List<SerializedEdge> edges;
	
	public DisjunctiveGraphAST(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph) {
		nodes = serializeAllNodes(input, disjGraph);
		arcs = serializeArcs(input, disjGraph);
		edges = serializeEdges(input, disjGraph);
	}
	
	public List<String> getNodes() {
		return nodes;
	}
	
	public Map<String, List<String>> getArcs() {
		return arcs;
	}
	
	public List<SerializedEdge> getEdges() {
		return edges;
	}

	private static class SerializedEdge {
		private final List<String> left;
		private final List<String> right;

		public SerializedEdge(List<String> left, List<String> right) {
			this.left = left;
			this.right = right;
		}

		public List<String> getLeft() {
			return left;
		}

		public List<String> getRight() {
			return right;
		}
	}
	
	private String serializeNode(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph, int node) {
		TaskUnitNode taskUnitNode = input.nodeAt(disjGraph.toOriginalNode(node));
		return taskUnitNode.getActivity().getShortName() + ":" + PatternEntry.toValueString(taskUnitNode.getUnit().getPattern());
	}

	private List<String> serializeNodes(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph, IntStream nodes) {
		return nodes
			.mapToObj(n -> serializeNode(input, disjGraph, n))
			.collect(Collectors.toList());
	}

	private List<String> serializeAllNodes(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph) {
		return serializeNodes(input, disjGraph, IntStream.range(0, disjGraph.nodes()));
	}

	private List<String> serializeNodes(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph, BitSet nodes) {
		return serializeNodes(input, disjGraph, nodes.stream());
	}
	
	/** Returns a textual representation of the given graph. */
	private Map<String, List<String>> serializeArcs(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph) {
		BitSet[] arcs = disjGraph.arcs();
		HashMap<String, List<String>> arcMap = new HashMap<>();
		for (int nSrc = 0; nSrc < arcs.length; nSrc++) {
			String src = serializeNode(input, disjGraph, nSrc);
			List<String> targets = arcs[nSrc].stream()
				.mapToObj(nDst -> serializeNode(input, disjGraph, nDst))
				.collect(Collectors.toList());
			arcMap.put(src, targets);
		}
		return arcMap;
	}
	
	/** Returns a textual representation of the given graph. */
	private List<SerializedEdge> serializeEdges(ModelToGraphTranslator.Input input, DisjunctiveGraph disjGraph) {
		return disjGraph.edges().stream()
			.map(e -> new SerializedEdge(
				serializeNodes(input, disjGraph, e.getLeft()),
				serializeNodes(input, disjGraph, e.getRight()))
			)
			.collect(Collectors.toList());
	}

}
