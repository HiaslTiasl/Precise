package it.unibz.precise.smv;

import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import it.unibz.precise.check.ModelToGraphTranslator;
import it.unibz.precise.check.TaskUnitNode;
import it.unibz.precise.graph.disj.DisjunctiveEdge;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.PatternEntry;

/**
 * Helper class wrapping a given {@link DisjunctiveGraph} to be translated to a NuSMV module.
 * 
 * @author MatthiasP
 *
 */
public class NuSMVContext {
	
	/**
	 * Enumeration of the 2 events "start" and "end" as well as
	 * their monotonic counterparts "started" and "ended".
	 */
	public enum VariableKind {
		start, started, end, ended;
		
		/** Returns a variable name that is a combination of this kind and the given node. */
		public String in(String node) {
			return name() + VAR_SEPARATOR + node;
		}
	}

	private static final String VAR_SEPARATOR = "_";		// Separates segments (task and location values) in variables

	private final ModelToGraphTranslator.Input input;
	private final DisjunctiveGraph graph;		// The underlying graph
	private final String[] nodeNames;		// Map from node to their names used in the SMV file
	
	public NuSMVContext(ModelToGraphTranslator.Input input, DisjunctiveGraph graph) {
		this.input = input;
		this.graph = graph;
		this.nodeNames = IntStream.range(0, graph.nodes())
			.mapToObj(this::nodeName)
			.toArray(String[]::new);
	}
	
	public String toNuSMV() {
		
		Stream<String> variables = Arrays.stream(nodeNames)
			.flatMap(this::declareVariables);
		
		Stream<String> assignments = Arrays.stream(nodeNames)
			.flatMap(this::assignVariables);
		
		Stream<String> existences = Arrays.stream(nodeNames)
			.map(this::existence);
		
		Stream<String> arcs = IntStream.range(0, graph.nodes())
			.boxed()
			.flatMap(s -> graph.allSuccessors(s).stream().mapToObj(t -> precedes(s, t)));
		
		Stream<String> edges = graph.edges().stream()
			.map(this::excludes);
		
		Stream<String> specConjuncts = Stream.of(existences, arcs, edges).flatMap(Function.identity());
		
		return "MODULE main\n"
			+ "VAR\n"
			+ lines(variables)
			+ "ASSIGN\n"
			+ lines(assignments)
			+ "LTLSPEC !("
			+ conjunction(specConjuncts)
			+ ");";
	}

	/** Returns a name of the given node. */
	private String nodeName(int node) {
		TaskUnitNode tuNode = input.nodeAt(graph.toOriginalNode(node));
		return tuNode.getActivity().getShortName()
			+ NuSMVContext.VAR_SEPARATOR
			+ PatternEntry.toValueString(tuNode.getUnit().getPattern(), NuSMVContext.VAR_SEPARATOR);
	}
	
	/** Returns a stream of variable declarations for the given node. */
	private Stream<String> declareVariables(String node) {
		// All VariableKinds of node as boolean variables
		return Stream.of(VariableKind.values())
			.map(eventType -> eventType.in(node))
			.map(this::declareBoolean);
	}
	
	/** Returns a stream of variable assignments for the given node. */
	private Stream<String> assignVariables(String node) {
		return Stream.concat(
			initVariables(node),
			assignNextVariables(node)
		);
	}
	
	/** Returns a stream of init-assignments of the given node. */
	private Stream<String> initVariables(String node) {
		// It is not possible that execution of a node is finished since the beginning.
		// This is not captured by next-assignments.
		// Starting one or more nodes in the beginning is possible if the LTL formula is not
		// violated.
		return Stream.of(VariableKind.end, VariableKind.ended)
			.map(variableType -> variableType.in(node))
			.map(v -> assign(init(v), "FALSE"));
	}
	
	/** Returns a stream of next-assignments of the given node. */
	private Stream<String> assignNextVariables(String node) {
		return Stream.of(
			assignNextStart(node),
			assignNextStarted(node),
			assignNextEnd(node),
			assignNextEnded(node)
		);
	}
	
	/** Returns a boolean variable declaration. */
	private String declareBoolean(String variable) {
		return variable + ": boolean";
	}
	
	/** Returns the identifier of the initial value of the given variable. */
	private String init(String variable) {
		return "init(" + variable + ")";
	}

	/** Returns the identifier of the next value of the given variable. */
	private String next(String variable) {
		return "next(" + variable + ")";
	}
	
	/** Returns an assignment composed of the given right and left side. */
	private String assign(String left, String right) {
		return left + " := " + right;
	}
	
	/** Assigns the next value of the start variable of the given node. */
	private String assignNextStart(String node) {
		// Execution of a node can only start once
		return assign(
			next(start(node)), started(node) + " ? FALSE : { TRUE, FALSE }"
		);
	}
	
	/** Assigns the next value of the started variable of the given node. */
	private String assignNextStarted(String node) {
		// Execution of a node has been started if it has been started previously or if it starts now
		return assign(next(started(node)), started(node) + " | " + next(start(node)));
	}
	
	/** Assigns the next value of the end variable of the given node. */
	private String assignNextEnd(String node) {
		// Execution of a node can and only once, and only after it has been started
		return assign(
			next(end(node)),
			started(node) + " & ! " + ended(node) + " ? { TRUE, FALSE } : FALSE"
		);
	}
	
	/** Assigns the next value of the ended variable of the given node. */
	private String assignNextEnded(String node) {
		// Execution of a node has been started if it has been started previously or if it starts now
		return assign(next(ended(node)), ended(node) + " | " + next(end(node)));
	}
	
	/** Returns the given lines as statements. */
	private String lines(Stream<String> lines) {
		return lines.map(l -> '\t' + l + ";\n\n")
			.collect(Collectors.joining());
	}
	
	/** Returns the given lines as a conjunction over multiple lines. */
	private String conjunction(Stream<String> lines) {
		return lines
			.map(NuSMVContext::parenthesis)
			.collect(Collectors.joining(")\n\t& (", "\n\t(", ")\n"));
	}
	
	/** Returns an LTL formula capturing the existence constraint of the given node. */
	private String existence(String node) {
		// The node must start and end
		return "F " + start(node) + " & F " + end(node);
	}
	
	/** Returns an LTL formula capturing a precedence constraint from {@code sourceNode} to {@code targetNode}. */
	private String precedes(int sourceNode, int targetNode) {
		String source = nodeNames[graph.toOriginalNode(sourceNode)];
		String target = nodeNames[graph.toOriginalNode(targetNode)];
		// The target node cannot start until the source node ends
		return "! " + start(target) + " U " + end(source);
	}
	
	/** Returns an LTL formula capturing the given disjunctive edge. */
	private String excludes(DisjunctiveEdge edge) {
		BitSet left = edge.getLeft(), right = edge.getRight();
		// Either all left precede all right or vice-versa
		return parenthesis(precedeAll(left, right))
			+ " | "
			+ parenthesis(precedeAll(right, left));
	}
	
	/**
	 * Returns an LTL formula capturing a precedence constraint from all nodes in
	 * {@code sources} to all nodes in {@code targets}.
	 */
	private String precedeAll(BitSet sources, BitSet targets) {
		return sources.stream()
			.boxed()
			.flatMap(s -> targets.stream().mapToObj(t -> precedes(s, t)))	// TODO: Delete cast once Eclipse understands
			.collect(Collectors.joining(") & (", "(", ")"));
	}
	
	/** Wraps the given formula in parenthesis. */
	private static String parenthesis(String formula) {
		return "(" + formula + ")";
	}
	
	/** Returns a variable indicating whether execution of the given node starts now. */
	private static String start(String node) {
		return VariableKind.start.in(node);
	}
	
	/** Returns a variable indicating whether execution of the given node has been started. */
	private static String started(String node) {
		return VariableKind.started.in(node);
	}
	
	/** Returns a variable indicating whether execution of the given node ends now. */
	private static String end(String node) {
		return VariableKind.end.in(node);
	}
	
	/** Returns a variable indicating whether execution of the given node has been ended. */
	private static String ended(String node) {
		return VariableKind.ended.in(node);
	}

}
