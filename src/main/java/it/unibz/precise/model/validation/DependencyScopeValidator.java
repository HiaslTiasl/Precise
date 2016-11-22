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

public class DependencyScopeValidator implements ConstraintValidator<WellDefinedScope, Dependency> {

	@Override
	public void initialize(WellDefinedScope constraintAnnotation) {
	}

	@Override
	public boolean isValid(Dependency dependency, ConstraintValidatorContext context) {
		Set<Attribute> notAllowedAttrs = dependency.getNotAllowedScopeAttributes().collect(Collectors.toSet());
		boolean valid = notAllowedAttrs.isEmpty();
		if (!valid) {
			context.disableDefaultConstraintViolation();
//			context.buildConstraintViolationWithTemplate(
//				context.getDefaultConstraintMessageTemplate()
//			)
//			.addPropertyNode("scope")
//			.addBeanNode()
//			.addConstraintViolation();
//		}
			Scope scope = dependency.getScope();
			List<Attribute> attrs = scope == null ? null : scope.getAttributes();
			int len = Util.size(attrs);
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
		
		return valid;
	}

}
