package it.unibz.precise.model;

/**
 * An object having an id of type long.
 * 
 * Used as a common interface of entities and projections where required
 * to handle them uniformly in custom resource processors.
 * 
 * @author MatthiasP
 *
 */
public interface HasLongId {
	long getId();
}
