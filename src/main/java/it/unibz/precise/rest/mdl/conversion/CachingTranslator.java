package it.unibz.precise.rest.mdl.conversion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachingTranslator<E, MDL> extends AbstractMDLTranslator<E, MDL> {
	
	private final MDLTranslator<E, MDL> delegate;
	
	private final Map<Object, MDL> entitiesToMDL;
	private final Map<Object, E> mdlToEntities;
	
	private Function<E, Object> entityKeyMapper;
	private Function<MDL, Object> mdlKeyMapper;

	private boolean cacheInverseDirection;
	private boolean sealed;

	public CachingTranslator(MDLTranslator<E, MDL> delegate, Function<E, Object> entityKeyMapper, Function<MDL, Object> mdlKeyMapper) {
		super(delegate.context());
		this.delegate = delegate;
		entitiesToMDL = new ConcurrentHashMap<>();
		mdlToEntities = new ConcurrentHashMap<>();
		usingKeys(entityKeyMapper, mdlKeyMapper);
	}
	
	public static <E, MDL> CachingTranslator<E, MDL> cache(MDLTranslator<E, MDL> delegate) {
		return cache(delegate, e -> e, mdl -> mdl);
	}
	
	public static <E, MDL> CachingTranslator<E, MDL> cache(
		MDLTranslator<E, MDL> delegate,
		Function<E, Object> entityKey,
		Function<MDL, Object> mdlKey
	) {
		return new CachingTranslator<>(delegate, entityKey, mdlKey);
	}
	
	public CachingTranslator<E, MDL> usingKeys(Function<E, Object> entityKeyMapper, Function<MDL, Object> mdlKeyMapper) {
		this.entityKeyMapper = entityKeyMapper;
		this.mdlKeyMapper = mdlKeyMapper;
		entitiesToMDL.clear();
		mdlToEntities.clear();
		return this;
	}
	
	public CachingTranslator<E, MDL> cacheInverseDirection(boolean cacheInverseDirection) {
		this.cacheInverseDirection = cacheInverseDirection;
		return this;
	}
	
	public CachingTranslator<E, MDL> seal() {
		this.sealed = true;
		return this;
	}
	
	@Override
	public MDL toMDL(E entity) {
		MDL mdl = entity == null ? null
			: convertAndCache(entity, entitiesToMDL, entityKeyMapper, delegate::toMDL);
		if (mdl != null && cacheInverseDirection)
			convertAndCache(mdl, mdlToEntities, mdlKeyMapper, k -> entity);
		return mdl;
	}
	
	@Override
	public E toEntity(MDL mdl) {
		E entity = mdl == null ? null
			: convertAndCache(mdl, mdlToEntities, mdlKeyMapper, delegate::toEntity);
		if (entity != null && cacheInverseDirection)
			convertAndCache(entity, entitiesToMDL, entityKeyMapper, k -> mdl);
		return entity;
	}
	
	private <T, R> R convertAndCache(T input, Map<Object, R> cache, Function<T, Object> keyMapper, Function<T, R> delegateMethod) {
		Object key = keyMapper.apply(input);
		return sealed ? cache.get(key) : cache.computeIfAbsent(key, k -> delegateMethod.apply(input));
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
