package it.unibz.precise;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.webmvc.RepositoryController;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.data.rest.webmvc.support.ExceptionMessage;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import it.unibz.precise.model.PreciseException;
import it.unibz.precise.model.validation.ExceptionDataExtractor;
import it.unibz.precise.rest.MDLFileController;

/**
 * Handler for exceptions thrown in REST controllers.
 * Exception are caught from {@link org.springframework.data.rest.webmvc.AbstractRepositoryRestController}
 * or from MDL-controllers.
 * 
 * @author MatthiasP
 *
 */
@ControllerAdvice(basePackageClasses = {RepositoryController.class, MDLFileController.class})
public class DefaultExceptionHandler {
	
	private ExceptionDataExtractor<DataAccessException, String[]> uniquenessExceptionResolver;	// maps UniquenessConstraint exceptions to the corresponding fields  
	private MessageSourceAccessor messageSourceAccessor;
	
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	DefaultExceptionHandler(
		ApplicationContext applicationContext,
		ExceptionDataExtractor<DataAccessException, String[]> uniquenessExceptionResolver
	) {
		messageSourceAccessor = new MessageSourceAccessor(applicationContext);
		this.uniquenessExceptionResolver = uniquenessExceptionResolver;
	}
	
	/**
	 * Return BAD_REQUEST and a list of validation errors.
	 * 
	 * Copied from {@link RepositoryRestExceptionHandler} to ensure that {@link #handle(DataAccessException)}
	 * does not handle {@link RepositoryConstraintViolationException}.
	 * 
	 * @see ValidationError
	 */
	@ExceptionHandler
	ResponseEntity<RepositoryConstraintViolationExceptionMessage> handleRepositoryConstraintViolationException(
			RepositoryConstraintViolationException o_O)
	{
		return ResponseEntity.badRequest()
			.body(new RepositoryConstraintViolationExceptionMessage(o_O, messageSourceAccessor));
	}
    
	/**
	 * Return CONFLICT and an error message.
	 * If the conflict is due to a UniquenessConstraint, prepare a custom message reporting the involved fields.
	 */
	@ExceptionHandler
	public ResponseEntity<ExceptionMessage> handle(DataAccessException e) {
		String[] uniqFields = uniquenessExceptionResolver.apply(e);
		Throwable actualReason = uniqFields != null
			? new DuplicateKeyException(Arrays.toString(uniqFields) +  " must be unique")
			: e.getMostSpecificCause();
		return handle(actualReason, HttpStatus.CONFLICT);
	}
	
	/** Handle the actual cause of the exception. */
	@ExceptionHandler
	public void handle(InvocationTargetException e) throws Throwable {
		throw e.getCause();
	}

	/** Use the exception message as is. */
	@ExceptionHandler
	public ResponseEntity<ExceptionMessage> handle(PreciseException e) {
		return handle(e, HttpStatus.BAD_REQUEST);
	}

	/** Unexpected errors. */
	//@ExceptionHandler
	public ResponseEntity<ExceptionMessage> handle(Throwable t) {
		return handle(t, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/** Return a corresponding {@link ExceptionMessage}. */
	private ResponseEntity<ExceptionMessage> handle(Throwable t, HttpStatus status) {
		logger.debug(t);
		return ResponseEntity.status(status).body(new ExceptionMessage(t));
	}
	
}