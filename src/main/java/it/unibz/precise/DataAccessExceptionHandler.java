package it.unibz.precise;

import java.util.Arrays;

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

import it.unibz.precise.model.validation.ExceptionResolver;
import it.unibz.precise.rest.MDLFileController;

@ControllerAdvice(basePackageClasses = {RepositoryController.class, MDLFileController.class})
public class DataAccessExceptionHandler {
	
	private ExceptionResolver<DataAccessException, String[]> uniquenessExceptionResolver;
	private MessageSourceAccessor messageSourceAccessor;
	
	@Autowired
	DataAccessExceptionHandler(
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
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ExceptionMessage(uniqFields != null
				? new DuplicateKeyException(Arrays.toString(uniqFields) +  " must be unique")
				: e
			));
	}
	
}