package it.unibz.precise.model.validation;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.ValidationErrors;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class ValidationAdapter {
	
	@Autowired
	private Validator validator;
	@Autowired
	private ObjectFactory<PersistentEntities> entities;
	
	public Errors validate(Object object) {
		ValidationErrors errors = new ValidationErrors(object, entities.getObject());
		validator.validate(object, errors);
		return errors;
	}
}
