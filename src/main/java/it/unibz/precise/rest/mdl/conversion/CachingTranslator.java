package it.unibz.precise.rest.mdl.conversion;

import java.util.HashMap;
import java.util.Map;

public class CachingTranslator<E, MDL> extends AbstractMDLTranslator<E, MDL> {
	
	private final MDLTranslator<E, MDL> delegate;
	
	private final Map<E, MDL> entitiesToMDL;
	private final Map<MDL, E> mdlToEntities;

	public CachingTranslator(MDLTranslator<E, MDL> delegate) {
		super(delegate.context());
		this.delegate = delegate;
		entitiesToMDL = new HashMap<>();
		mdlToEntities = new HashMap<>();
	}

	@Override
	public MDL toMDL(E entity) {
		return entitiesToMDL.computeIfAbsent(entity, e -> delegate.toMDL(e));
	}

	@Override
	public E toEntity(MDL mdl) {
		return mdlToEntities.computeIfAbsent(mdl, m -> delegate.toEntity(m));
	}
	
	public void clearCache() {
		entitiesToMDL.clear();
		mdlToEntities.clear();
	}

	@Override
	public E createEntity(MDL mdl) {
		return delegate.createEntity(null);
	}

	@Override
	public MDL createMDL(E entity) {
		return delegate.createMDL(null);
	}

	@Override
	public void updateEntity(MDL mdl, E entity) {
		delegate.updateEntity(mdl, entity);
	}

	@Override
	public void updateMDL(E entity, MDL mdl) {
		delegate.updateMDL(entity, mdl);
	}

}
