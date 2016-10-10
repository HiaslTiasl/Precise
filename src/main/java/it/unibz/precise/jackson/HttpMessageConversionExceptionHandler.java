package it.unibz.precise.jackson;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.data.rest.webmvc.support.ExceptionMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.UnresolvedId;

import it.unibz.precise.model.InvalidLocationException;
import it.unibz.precise.rest.MDLFileController;

@ControllerAdvice(basePackageClasses = {RepositoryRestExceptionHandler.class, MDLFileController.class})
public class HttpMessageConversionExceptionHandler {

    @ExceptionHandler
    ResponseEntity<ExceptionMessage> handle(HttpMessageConversionException e) {
    	return ResponseEntity.badRequest().body(new ExceptionMessage(mapException(e.getMostSpecificCause())));
    }
    
    @ExceptionHandler
    ResponseEntity<ExceptionMessage> handle(InvalidLocationException e) {
    	return ResponseEntity.badRequest().body(new ExceptionMessage(e));
    }
    
    private static Throwable mapException(Throwable e) {
    	if (e instanceof UnresolvedForwardReference)
    		e = new Exception(serialize((UnresolvedForwardReference)e));
    	else if (e instanceof JsonProcessingException)
    		e = new Exception(serialize((JsonProcessingException)e));
    	return e;
    }
    
    private static String serialize(UnresolvedForwardReference ufr) {
    	String msg = ufr.getOriginalMessage();
    	List<UnresolvedId> ids = ufr.getUnresolvedIds();
        if (ids != null) {
        	msg += ids.stream()
        		.map(unresolvedId -> serialize("Object ID [" + unresolvedId.getId() + "]", unresolvedId.getLocation()))
        		.collect(Collectors.joining(", "));
        }
        return msg;
    }
    
    private static String serialize(String msg, JsonLocation jsonLoc) {
    	return jsonLoc == null ? msg : msg + " at " + serialize(jsonLoc);
    }

    private static String serialize(JsonProcessingException jpe) {
		return serialize(jpe.getOriginalMessage(), jpe.getLocation());
    }
    
    private static String serialize(JsonLocation jsonLoc) {
		return "line " + jsonLoc.getLineNr()
			+ " column " + jsonLoc.getColumnNr();
    }
    
}