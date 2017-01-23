package it.unibz.precise.rest.mdl.conversion;

/**
 * Basic interface for defining translations between MDL and Entity representations.
 * 
 * Every translation from one representation into the other (i.e. {@link #toEntity(Object)} and {@link #toMDL(Object)})
 * consists of two steps:
 * <ol>
 * 	<li> Create an instance of the target type (see {@link #createEntity()}, {@link #createMDL()})
 * 	<li> Update the created instance based on the input (see {@link #updateEntity()}, {@link #updateMDL()})
 * </ol>
 * To maximize code reuse, these individual steps are all accessible through this interface.
 * 
 * @author MatthiasP
 *
 * @param <E> The entity type
 * @param <MDL> The MDL representation type
 */
public interface MDLTranslator<E, MDL> {
	
	/** Returns the {@link MDLContext} of the translation. */
	MDLContext context();
	
	/** Create a new entity. */
	E createEntity();
	/** Create a new MDL representation. */
	MDL createMDL();
	
	/** Update {@code entity} based on {@code mdl}. */
	void updateEntity(MDL mdl, E entity);
	/** Update {@code mdl} based on {@code entity}. */
	void updateMDL(E entity, MDL mdl);

	/** Translate the given entity into the corresponding MDL representation. */
	MDL toMDL(E entity);
	/** Translate the given MDL representation into the corresponding entity. */
	E toEntity(MDL mdl);
	
}
