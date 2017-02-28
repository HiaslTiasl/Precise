package it.unibz.precise.check;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.Activity;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

/**
 * Checks if all activities in a model have a phase associated.
 * 
 * @author MatthiasP
 *
 */
@Service
public class MissingPhaseChecker implements ProblemChecker {
	
	public static final String PROBLEM_TYPE = "missingPhase";

	public static final String PROBLEM_MESSAGE = "Activity {0} has no phase.";

	@Override
	public Category getCategory() {
		return Category.STRUCTURE_ERROR;
	}
	
	@Override
	public String getType() {
		return PROBLEM_TYPE;
	}
	
	@Override
	public Stream<ModelProblem> check(Model model) {
		return model.getActivities().stream()
			.map(this::check)
			.filter(Objects::nonNull);
	}
	
	/**
	 * Returns a {@link ModelProblem} if {@code activity} is used in the diagram but has no phase,
	 * otherwise returns null.
	 */
	private ModelProblem check(Activity activity) {
		List<Task> tasks = activity.getTasks();
		// If there are no tasks, then the activity is not used in the diagram.
		return activity.getPhase() != null || tasks.isEmpty() ? null
			: warning(MessageFormat.format(PROBLEM_MESSAGE, activity.getName()), tasks, null);
	}

}
