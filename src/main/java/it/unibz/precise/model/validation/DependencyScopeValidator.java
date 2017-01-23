package it.unibz.precise.model.validation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Scope;
import it.unibz.util.Util;

/**
 * Validates the scope of a dependency based on the phases of source and target tasks.
 * The scope of the dependency must only contain attributes that are available in both
 * phases of the two tasks.
 * 
 * @author MatthiasP
 * @see Dependency#getNotAllowedScopeAttributes()
 *
 */
public class DependencyScopeValidator implements ConstraintValidator<WellDefinedScope, Dependency> {

	@Override
	public void initialize(WellDefinedScope constraintAnnotation) {
	}

	@Override
	public boolean isValid(Dependency dependency, ConstraintValidatorContext context) {
		// Get the set of not allowed attributes in the scope of the dependency.
		// The check succeeds iff the obtained set is empty.
		Set<Attribute> notAllowedAttrs = dependency.getNotAllowedScopeAttributes().collect(Collectors.toSet());
		boolean valid = notAllowedAttrs.isEmpty();
		if (!valid)
			addConstraintViolations(dependency, context, notAllowedAttrs);
		return valid;
	}
	
	/**
	 * Add a constraint violation for each not allowed attribute,
	 * indicating the corresponding path starting from the dependency.
	 * <p>
	 * Note: We report the name of the attribute rather than the attribute
	 * itself to avoid an infinite loop in the deserialization of the attribute
	 * value (attribute -> model -> attribute -> ...).
	 * This makes sense from the value's perspective since reporting the name
	 * of the problematic attribute is sufficient to understand and fix the problem.
	 * However, also the path points to the attribute's name, which suggests that
	 * the problem is not with the attribute itself, but only with its name.
	 * For more details see <a href="http://stackoverflow.com/questions/40691393/responses-of-class-level-jsr-303-constraint-violations-in-spring-data-rest">
	 * 	http://stackoverflow.com/questions/40691393/responses-of-class-level-jsr-303-constraint-violations-in-spring-data-rest
	 * </a>
	 */
	private void addConstraintViolations(Dependency dependency, ConstraintValidatorContext context, Set<Attribute> notAllowedAttrs) {
		// Do not include default violation, our custom violations are more precise
		context.disableDefaultConstraintViolation();
		Scope scope = dependency.getScope();
		List<Attribute> attrs = scope == null ? null : scope.getAttributes();
		int len = Util.size(attrs);
		
		// N.B.: Iterating over all attributes instead of only notAllowedAttrs
		// to obtain the correct indices
		for (int i = 0; i < len; i++) {
			Attribute attr = attrs.get(i);
			if (notAllowedAttrs.contains(attr)) {
				context.buildConstraintViolationWithTemplate(
					context.getDefaultConstraintMessageTemplate()
				)
				.addPropertyNode("scope")
				.addPropertyNode("attributes")
				.addPropertyNode("name")
					.inIterable().atIndex(i)
				.addBeanNode()
				.addConstraintViolation();
			}
			
		}
	}

}
