package it.unibz.precise.rest;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackageClasses = MDLExceptionHandler.class)
public class MDLExceptionHandler {
	
	@ExceptionHandler
	@ResponseBody
	ResponseEntity<?> handleNotFound(ResourceNotFoundException e) {
		return ResponseEntity.notFound().build();
	}
	
}
