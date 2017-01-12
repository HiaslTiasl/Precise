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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import it.unibz.precise.model.PreciseException;
import it.unibz.precise.model.validation.ExceptionResolver;
import it.unibz.precise.rest.MDLFileController;

@ControllerAdvice(basePackageClasses = {RepositoryController.class, MDLFileController.class})
public class DefaultExceptionHandler {
	
	private ExceptionResolver<DataAccessException, String[]> uniquenessExceptionResolver;
	private MessageSourceAccessor messageSourceAccessor;
	
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	DefaultExceptionHandler(
		ApplicationContext applicationContext,
		ExceptionResolver<DataAccessException, String[]> uniquenessExceptionResolver
	) {
		messageSourceAccessor = new MessageSourceAccessor(applicationContext);
		this.uniquenessExceptionResolver = uniquenessExceptionResolver;
	}
	
	/**
	 * Copied from {@link RepositoryRestExceptionHandler} to ensure that {@link #handle(DataAccessException)}
	 * does not handle {@link RepositoryConstraintViolationException}.
	 */
	@ExceptionHandler
	ResponseEntity<RepositoryConstraintViolationExceptionMessage> handleRepositoryConstraintViolationException(
			RepositoryConstraintViolationException o_O)
	{
		return ResponseEntity.badRequest()
			.body(new RepositoryConstraintViolationExceptionMessage(o_O, messageSourceAccessor));
	}
    
	@ExceptionHandler
	public ResponseEntity<ExceptionMessage> handle(DataAccessException e) {
		String[] uniqFields = uniquenessExceptionResolver.resolve(e);
		Throwable actualReason = uniqFields != null
			? new DuplicateKeyException(Arrays.toString(uniqFields) +  " must be unique")
			: e.getMostSpecificCause();
		return handle(actualReason, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler
	public void handle(InvocationTargetException e) throws Throwable {
		throw e.getCause();
	}

	@ExceptionHandler
	public ResponseEntity<ExceptionMessage> handle(PreciseException e) {
		return handle(e, HttpStatus.BAD_REQUEST);
	}

	//@ExceptionHandler
	public ResponseEntity<ExceptionMessage> handle(Throwable t) {
		return handle(t, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	private ResponseEntity<ExceptionMessage> handle(Throwable t, HttpStatus status) {
		logger.debug(t);
		return ResponseEntity.status(status).body(new ExceptionMessage(t));
	}
	
}