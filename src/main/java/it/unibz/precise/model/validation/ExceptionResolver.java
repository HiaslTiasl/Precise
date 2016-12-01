package it.unibz.precise.model.validation;

public interface ExceptionResolver<E extends Exception, T> {

	T resolve(E exc);
	
}
