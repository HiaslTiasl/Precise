package it.unibz.precise.check;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

/**
 * Checks whether all {@link Dependency}s have both a source and a target task.
 * 
 * @author MatthiasP
 *
 */
@Service
public class MissingDependencyEndpointsChecker implements ProblemChecker {

	public static final String PROBLEM_TYPE = "missingDepencyEndpoints";

	public static final String PROBLEM_MESSAGE_NO_SOURCE   = "Missing source task for dependency.";
	public static final String PROBLEM_MESSAGE_NO_TARGET   = "Missing target task for dependency.";
	public static final String PROBLEM_MESSAGE_NO_ENDPOINT = "Missing source and target tasks for dependency.";

	@Override
	public Category getCategory() {
		return Category.STRUCTURE_WARNING;
	}
	
	@Override
	public String getType() {
		return PROBLEM_TYPE;
	}
	
	@Override
	public Stream<ModelProblem> check(Model model) {
		return model.getDependencies().stream()
			.map(this::check)				// Check each dependencies
			.filter(Objects::nonNull);		// filter out empty warnings
	}
	
	/** Return a ConsistencyWarning if {@code d} is missing a source of a target, otherwise return null. */
	private ModelProblem check(Dependency d) {
		boolean missingSource = d.getSource() == null;
		boolean missingTarget = d.getTarget() == null;
		
		return missingSource && missingTarget ? warning(PROBLEM_MESSAGE_NO_ENDPOINT, d)
			: missingSource ? warning(PROBLEM_MESSAGE_NO_SOURCE, d)
			: missingTarget ? warning(PROBLEM_MESSAGE_NO_TARGET, d)
			: null;
	}
	
	/**
	 * Produce a warning for the given template and dependency.
	 * @see ProblemChecker#warning(String, java.util.List, java.util.List)
	 */
	private ModelProblem warning(String msgTemplate, Dependency d) {
		return warning(MessageFormat.format(msgTemplate, d), Arrays.asList(d), null);
	}

}
