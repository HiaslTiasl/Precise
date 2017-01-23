package it.unibz.precise.model.validation;

import java.util.function.Function;

/**
 * 
 * @author MatthiasP
 *
 * @param <E>
 * @param <T>
 */
public interface ExceptionDataExtractor<E extends Exception, T> extends Function<E,T> {

}
