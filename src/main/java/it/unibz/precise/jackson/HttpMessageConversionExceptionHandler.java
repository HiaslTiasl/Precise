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
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.UnresolvedId;

import it.unibz.precise.rest.MDLFileController;

/**
 * ExceptionHandler for HTTP conversions with customized error messages.
 * 
 * @author MatthiasP
 * @see DeserializationProblemHandler
 */
@ControllerAdvice(basePackageClasses = {RepositoryRestExceptionHandler.class, MDLFileController.class})
public class HttpMessageConversionExceptionHandler {

	/** Handle the given exception according to the most specific cause, responding with BAD_REQUEST. */
    @ExceptionHandler
    ResponseEntity<ExceptionMessage> handle(HttpMessageConversionException e) {
    	return ResponseEntity.badRequest().body(new ExceptionMessage(mapException(e.getMostSpecificCause())));
    }
    
    /** Convert the given exception to a new exception with a custom message if applicable. */
    private static Throwable mapException(Throwable e) {
    	if (e instanceof UnresolvedForwardReference)
    		e = new Exception(serialize((UnresolvedForwardReference)e));
    	else if (e instanceof JsonProcessingException)
    		e = new Exception(serialize((JsonProcessingException)e));
    	return e;
    }
    
    /** Returns a custom message for the given {@link UnresolvedForwardReference} to avoid Java class names in the message. */
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
    
    /** Appends a textual description of {@code jsonLoc} to {@code msg}, if available. */
    private static String serialize(String msg, JsonLocation jsonLoc) {
    	return jsonLoc == null ? msg : msg + " at " + serialize(jsonLoc);
    }

    /**
     * Returns a textual description of the given {@link JsonProcessingException} to avoid a
     * {@link JsonLocation#getSourceRef() sourceRef} in the message.
     */
    private static String serialize(JsonProcessingException jpe) {
		return serialize(jpe.getOriginalMessage(), jpe.getLocation());
    }
    
    /** Returns a textual description of the given {@link JsonLocation}, indicating line and column numbers. */
    private static String serialize(JsonLocation jsonLoc) {
		return "line " + jsonLoc.getLineNr()
			+ " column " + jsonLoc.getColumnNr();
    }
    
}