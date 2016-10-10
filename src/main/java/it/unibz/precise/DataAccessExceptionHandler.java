package it.unibz.precise;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.webmvc.RepositoryController;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.data.rest.webmvc.support.ExceptionMessage;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import it.unibz.precise.rest.MDLFileController;

@ControllerAdvice(basePackageClasses = {RepositoryController.class, MDLFileController.class})
public class DataAccessExceptionHandler {
	
	private MessageSourceAccessor messageSourceAccessor;
	private Map<String, String> uniqueConstraintMappings;
	
	@Autowired
	DataAccessExceptionHandler(ApplicationContext applicationContext) {
		messageSourceAccessor = new MessageSourceAccessor(applicationContext);
		uniqueConstraintMappings = new HashMap<>();
		addUniqueConstraintMappings();
	}
	
	private void addUniqueConstraintMappings() {
		uniqueConstraintMappings.put("UC_MODEL_NAME", "name");
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
		String msg = e.getMessage();
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ExceptionMessage(
				uniqueConstraintMappings.entrySet().stream()
					.filter(entry -> msg.contains(entry.getKey()))
					.findAny()
					.map(entry -> new Exception(entry.getValue() + " must be unique"))
					.orElse(e)
			));
	}
	
}