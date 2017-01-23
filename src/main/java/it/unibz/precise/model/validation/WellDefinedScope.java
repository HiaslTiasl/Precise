package it.unibz.precise.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The scope must only contain attributes that are included in the phases of both
 * the source and the target.
 * 
 * @author MatthiasP
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=DependencyScopeValidator.class)
public @interface WellDefinedScope {
	
	String message() default "{it.unibz.precise.model.validation.WellDefinedScope.message}";

	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
