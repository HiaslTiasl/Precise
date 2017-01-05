package it.unibz.precise.check;

import it.unibz.precise.check.ConsistencyWarning.Category;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class MissingDependencyEndpointsChecker implements ConsistencyChecker {

	public static final String WARNING_TYPE = "missingDepencyEndpoints";

	public static final String WARNING_MESSAGE_NO_SOURCE   = "Missing source task for dependency.";
	public static final String WARNING_MESSAGE_NO_TARGET   = "Missing target task for dependency.";
	public static final String WARNING_MESSAGE_NO_ENDPOINT = "Missing source and target tasks for dependency.";

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
		return model.getDependencies().stream()
			.map(this::check)
			.filter(Objects::nonNull);
	}
	
	private ConsistencyWarning check(Dependency d) {
		boolean missingSource = d.getSource() == null;
		boolean missingTarget = d.getTarget() == null;
		
		return missingSource && missingTarget ? warning(WARNING_MESSAGE_NO_ENDPOINT, d)
			: missingSource ? warning(WARNING_MESSAGE_NO_SOURCE, d)
			: missingTarget ? warning(WARNING_MESSAGE_NO_TARGET, d)
			: null;
	}
	
	private ConsistencyWarning warning(String msgTemplate, Dependency d) {
		return warning(MessageFormat.format(msgTemplate, d), Arrays.asList(d), null);
	}

}
