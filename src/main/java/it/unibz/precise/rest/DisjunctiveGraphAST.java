package it.unibz.precise.rest;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.unibz.precise.check.TaskUnitNode;
import it.unibz.precise.graph.disj.Arc;
import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.PatternEntry;

public class DisjunctiveGraphAST {

	private Collection<String> nodes;
	private Collection<Arc<String>> arcs;
	private Collection<DisjunctiveEdge<String>> edges;
	
	public DisjunctiveGraphAST(DisjunctiveGraph<TaskUnitNode> disjGraph) {
		nodes = serializeNodes(disjGraph.nodes());
		arcs = serializeArcs(disjGraph.arcs());
		edges = serializeEdges(disjGraph.edges());
	}
	
	public Collection<String> getNodes() {
		return nodes;
	}
	
	public Collection<Arc<String>> getArcs() {
		return arcs;
	}
	
	public Collection<DisjunctiveEdge<String>> getEdges() {
		return edges;
	}
	
	
	private String serializeNode(TaskUnitNode node) {
		return node.getActivity().getShortName() + ":" + PatternEntry.toValueString(node.getUnit().getPattern());
	}
	
	private Set<String> serializeNodes(Collection<TaskUnitNode> nodes) {
		return nodes.stream()
			.map(this::serializeNode)
			.collect(Collectors.toSet());
	}
	
	/** Returns a textual representation of the given graph. */
	private List<Arc<String>> serializeArcs(Collection<Arc<TaskUnitNode>> arcs) {
		return arcs.stream()
			.map(a -> new Arc<>(serializeNode(a.getSource()), serializeNode(a.getTarget())))
			.collect(Collectors.toList());
	}
	
	/** Returns a textual representation of the given graph. */
	private List<DisjunctiveEdge<String>> serializeEdges(Collection<DisjunctiveEdge<TaskUnitNode>> edges) {
		return edges.stream()
			.map(e -> new DisjunctiveEdge<>(serializeNodes(e.getLeft()), serializeNodes(e.getRight())))
			.collect(Collectors.toList());
	}

}
