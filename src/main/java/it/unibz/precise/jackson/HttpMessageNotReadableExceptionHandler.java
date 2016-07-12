package it.unibz.precise.jackson;

import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;

import it.unibz.precise.rest.FullModel;

@ControllerAdvice(basePackageClasses = {RepositoryRestExceptionHandler.class, FullModel.class})
public class HttpMessageNotReadableExceptionHandler {

    @ExceptionHandler
    ResponseEntity<String> handle(HttpMessageNotReadableException e) {
    	return badRequest(getErrorMessage(e.getMostSpecificCause()));
    }
    
    private static String getErrorMessage(Throwable e) {
    	String errMsg;
    	if (e instanceof JsonProcessingException)
    		errMsg = serialize((JsonProcessingException)e);
    	else
    		errMsg = e.getMessage();
    	return errMsg;
    }
    
    private static String serialize(JsonProcessingException jpe) {
		return jpe.getOriginalMessage() + " at " + serialize(jpe.getLocation());
    }
    
    private static String serialize(JsonLocation jsonLoc) {
		return "line " + jsonLoc.getLineNr()
			+ " column " + jsonLoc.getColumnNr();
    }
    
    private static ResponseEntity<String> badRequest(String errMsg) {
    	return new ResponseEntity<>(errMsg, HttpStatus.BAD_REQUEST);
    }
}