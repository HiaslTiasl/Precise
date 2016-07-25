package it.unibz.precise.jackson;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.UnresolvedId;

import it.unibz.precise.rest.UserSyntaxController;

@ControllerAdvice(basePackageClasses = {RepositoryRestExceptionHandler.class, UserSyntaxController.class})
public class HttpMessageNotReadableExceptionHandler {

    @ExceptionHandler
    ResponseEntity<String> handle(HttpMessageNotReadableException e) {
    	return badRequest(getErrorMessage(e.getMostSpecificCause()));
    }
    
    private static String getErrorMessage(Throwable e) {
    	String errMsg;
    	if (e instanceof UnresolvedForwardReference)
    		errMsg = serialize((UnresolvedForwardReference)e);
    	if (e instanceof JsonProcessingException)
    		errMsg = serialize((JsonProcessingException)e);
    	else
    		errMsg = e.getMessage();
    	return errMsg;
    }
    
    private static String serialize(UnresolvedForwardReference ufr) {
    	String msg = ufr.getOriginalMessage();
    	List<UnresolvedId> ids = ufr.getUnresolvedIds();
        if (ids != null) {
        	msg += ids.stream()
        		.map(unresolvedId -> serialize(unresolvedId.getId().toString(), unresolvedId.getLocation()))
        		.collect(Collectors.joining(", "));
        }
        return msg;
    }
    
    private static String serialize(String msg, JsonLocation jsonLoc) {
    	return msg == null ? msg : msg + " at " + serialize(jsonLoc);
    }

    private static String serialize(JsonProcessingException jpe) {
		return serialize(jpe.getOriginalMessage(), jpe.getLocation());
    }
    
    private static String serialize(JsonLocation jsonLoc) {
		return "line " + jsonLoc.getLineNr()
			+ " column " + jsonLoc.getColumnNr();
    }
    
    private static ResponseEntity<String> badRequest(String errMsg) {
    	return new ResponseEntity<>(errMsg, HttpStatus.BAD_REQUEST);
    }
}