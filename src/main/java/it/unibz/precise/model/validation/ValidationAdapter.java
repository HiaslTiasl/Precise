package it.unibz.precise.model.validation;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.ValidationErrors;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Wraps {@link Validator} and automatically calls it with an appropriate {@link ValidationErrors}
 * instance.
 * 
 * This is useful in MDL-controllers since we have MDL-classes instead of Entities as parameters,
 * so we cannot ask Spring to automatically create corresponding Errors for us.
 * 
 * @author MatthiasP
 *
 */
@Service
public class ValidationAdapter {
	
	@Autowired
	private Validator validator;
	@Autowired
	private ObjectFactory<PersistentEntities> entitiesFactory;
	
	public Errors validate(Object object) {
		ValidationErrors errors = new ValidationErrors(object, entitiesFactory.getObject());
		validator.validate(object, errors);
		return errors;
	}
}
