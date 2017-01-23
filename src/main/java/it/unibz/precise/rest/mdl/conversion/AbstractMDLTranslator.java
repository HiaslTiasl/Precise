package it.unibz.precise.rest.mdl.conversion;

/**
 * Abstract base class of {@link MDLTranslator}s holding a {@link MDLContext}.
 * Implements the translation in terms of the corresponding create and translate methods.
 * Guards implementations of update methods in subclasses from null.
 * 
 * @author MatthiasP
 *
 * @param <E> The entity type.
 * @param <MDL> The MDL representation type.
 */
public abstract class AbstractMDLTranslator<E, MDL> implements MDLTranslator<E, MDL> {
	
	private final MDLContext context;
	
	AbstractMDLTranslator(MDLContext context) {
		this.context = context;
	}
	
	public MDLContext context() {
		return context;
	}
	
	/** Implementation of {@link #updateMDL(Object, Object)} that is called only with non-null values. */
	protected abstract void updateMDLImpl(E entity, MDL mdl);
	/** Implementation of {@link #updateEntity(Object, Object)} that is called only with non-null values. */
	protected abstract void updateEntityImpl(MDL mdl, E entity);

	@Override
	public void updateMDL(E entity, MDL mdl) {
		if (entity != null && mdl != null)
			updateMDLImpl(entity, mdl);
	}
	
	@Override
	public void updateEntity(MDL mdl, E entity) {
		if (mdl != null && entity != null )
			updateEntityImpl(mdl, entity);
		
	}
	
	/** Translates {@code entity} to an MDL representation using {@link #createMDL()} and {@link #updateMDL(Object, Object)}.*/
	@Override
	public MDL toMDL(E entity) {
		MDL mdl = entity == null ? null : createMDL();
		updateMDL(entity, mdl);
		return mdl;
	}
	
	/** Translates {@code mdl} to an entity using {@link #createEntity()} and {@link #updateEntity(Object, Object)}.*/
	@Override
	public E toEntity(MDL mdl) {
		E entity = mdl == null ? null : createEntity();
		updateEntity(mdl, entity);
		return entity;
	}
	
}
