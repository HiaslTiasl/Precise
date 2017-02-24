package it.unibz.precise.smv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unibz.precise.check.ModelToGraphTranslator;
import it.unibz.precise.check.TaskUnitNode;
import it.unibz.precise.graph.disj.DisjunctiveGraph;
import it.unibz.precise.model.Model;

/**
 * Translates a {@link Model} into a NuSMV module file.
 * 
 * @author MatthiasP
 *
 */
@Service
public class ModelToNuSMVTranslator {
	
	@Autowired
	private ModelToGraphTranslator translator;
	
	/** Returns the file content of a NuSMV module of the given {@link Model}. */
	public String translate(Model model, boolean ignoreSimpleEdges) {
		// Translate the model into a graph, and the graph into NuSMV
		DisjunctiveGraph<TaskUnitNode> disjunctiveGraph = translator.translate(model.getTasks(), ignoreSimpleEdges);
		NuSMVContext context = new NuSMVContext(disjunctiveGraph);
		return context.toNuSMV();
	}
	
	/** Returns the file content of a NuSMV module of the given {@link DisjunctiveGraph}. */
	public String translate(DisjunctiveGraph<TaskUnitNode> disjunctiveGraph) {
		NuSMVContext context = new NuSMVContext(disjunctiveGraph);
		return context.toNuSMV();
	}
	
}
