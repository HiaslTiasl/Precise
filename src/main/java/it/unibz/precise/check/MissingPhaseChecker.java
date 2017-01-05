package it.unibz.precise.check;

import it.unibz.precise.check.ConsistencyWarning.Category;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class MissingPhaseChecker implements ConsistencyChecker {
	
	public static final String WARNING_TYPE = "missingPhase";

	public static final String WARNING_MESSAGE = "Task definition {0} has no phase.";

	@Override
	public Category getCategory() {
		return Category.COMPLETENESS;
	}
	
	@Override
	public String getType() {
		return WARNING_TYPE;
	}
	
	@Override
	public Stream<ConsistencyWarning> check(Model model) {
		return model.getTaskTypes().stream()
			.map(this::check)
			.filter(Objects::nonNull);
	}
	
	private ConsistencyWarning check(TaskType taskDef) {
		List<Task> tasks = taskDef.getTasks();
		// Only give a warning if there is at least one box
		return taskDef.getPhase() != null || tasks.isEmpty() ? null
			: warning(MessageFormat.format(WARNING_MESSAGE, taskDef.getName()), tasks, null);
	}

}
