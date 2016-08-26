package it.unibz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.data.rest.webmvc.support.ExceptionMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Useful helper Methods copied from {@link RepositoryRestExceptionHandler}
 * for granting public access.
 * 
 * @author MatthiasP
 */
public class ResponseEntityHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(ResponseEntityHelper.class);

	public static ResponseEntity<?> notFound(HttpHeaders headers) {
		return response(HttpStatus.NOT_FOUND, headers, null);
	}

	public static ResponseEntity<ExceptionMessage> badRequest(HttpHeaders headers, Throwable throwable) {
		return errorResponse(HttpStatus.BAD_REQUEST, headers, throwable);
	}

	public static ResponseEntity<ExceptionMessage> errorResponse(HttpStatus status, HttpHeaders headers,
			Throwable exception) {

		if (exception != null) {

			String message = exception.getMessage();
			LOG.error(message, exception);

			if (StringUtils.hasText(message)) {
				return response(status, headers, new ExceptionMessage(exception));
			}
		}

		return response(status, headers, null);
	}

	public static <T> ResponseEntity<T> response(HttpStatus status, HttpHeaders headers) {
		return response(status, headers, null);
	}

	public static <T> ResponseEntity<T> response(HttpStatus status, HttpHeaders headers, T body) {

		Assert.notNull(headers, "Headers must not be null!");
		Assert.notNull(status, "HttpStatus must not be null!");

		return new ResponseEntity<T>(body, headers, status);
	}
	
}
