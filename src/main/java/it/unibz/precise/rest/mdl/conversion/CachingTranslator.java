package it.unibz.precise.rest.mdl.conversion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachingTranslator<E, MDL> extends AbstractMDLTranslator<E, MDL> {
	
	private final MDLTranslator<E, MDL> delegate;
	
	private final Map<E, MDL> entitiesToMDL;
	private final Map<MDL, E> mdlToEntities;

	public CachingTranslator(MDLTranslator<E, MDL> delegate) {
		super(delegate.context());
		this.delegate = delegate;
		entitiesToMDL = new ConcurrentHashMap<>();
		mdlToEntities = new ConcurrentHashMap<>();
	}
	
	@Override
	public MDL toMDL(E entity) {
		return entity == null ? null : entitiesToMDL.computeIfAbsent(entity, e -> delegate.toMDL(e));
	}

	@Override
	public E toEntity(MDL mdl) {
		return mdl == null ? null : mdlToEntities.computeIfAbsent(mdl, m -> delegate.toEntity(m));
	}
	
	public void clearCache() {
		entitiesToMDL.clear();
		mdlToEntities.clear();
	}

	@Override
	public E createEntity(MDL mdl) {
		return delegate.createEntity(mdl);
	}

	@Override
	public MDL createMDL(E entity) {
		return delegate.createMDL(entity);
	}

	@Override
	protected void updateEntityImpl(MDL mdl, E entity) {
		delegate.updateEntity(mdl, entity);
	}

	@Override
	protected void updateMDLImpl(E entity, MDL mdl) {
		delegate.updateMDL(entity, mdl);
	}

}
