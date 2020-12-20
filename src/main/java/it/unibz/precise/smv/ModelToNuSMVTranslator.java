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
		ModelToGraphTranslator.Input input = new ModelToGraphTranslator.Input(model);
		DisjunctiveGraph disjunctiveGraph = translator.translate(input, ignoreSimpleEdges);
		NuSMVContext context = new NuSMVContext(input, disjunctiveGraph);
		return context.toNuSMV();
	}
}
