package it.unibz.precise.rest.mdl.conversion;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A delegating {@link MDLTranslator} that caches the returned results.
 * 
 * Caching can be customized in the following ways:
 * <ul>
 * 	<li>{@link #usingKeys(Function, Function)}: Provide custom key functions for entities and MDLs for the cache entries (default is identity).
 * 	<li>{@link #cacheInverseDirection(boolean)}: Sync cache for both directions.
 * 	<li>{@link #seal(boolean)}: Prevent creation of new instances, i.e. translation returns cached instances if available or null otherwise.
 * </ul>
 * 
 * @author MatthiasP
 * @see MDLContext
 *
 * @param <E> The entity type.
 * @param <MDL> The MDL representation type.
 */
public class CachingTranslator<E, MDL> extends AbstractMDLTranslator<E, MDL> {
	
	private final MDLTranslator<E, MDL> delegate;					// The underlying translator whose results are to be cached
	
	private final ConcurrentHashMap<Object, MDL> entitiesToMDL;		// Cached MDLs by entity keys
	private final ConcurrentHashMap<Object, E> mdlToEntities;		// Cached entities by MDL keys
	
	private Function<E, Object> entityKeyMapper;					// Maps entities to cache keys
	private Function<MDL, Object> mdlKeyMapper;						// Maps MDLs to cache keys

	private boolean cacheInverseDirection;							// Sync the two caches?
	private boolean sealed;											// Return null if not cached?

	/** Create a new {@code CachingTranslator} for the given delegate. */
	CachingTranslator(MDLTranslator<E, MDL> delegate) {
		super(delegate.context());
		this.delegate = delegate;
		entitiesToMDL = new ConcurrentHashMap<>();
		mdlToEntities = new ConcurrentHashMap<>();
		this.entityKeyMapper = e -> e;
		this.mdlKeyMapper = mdl -> mdl;
	}
	
	/** Wrap the given {@link MDLTranslator} in a {@link CachingTranslator}. */
	public static <E, MDL> CachingTranslator<E, MDL> cache(MDLTranslator<E, MDL> delegate) {
		return new CachingTranslator<>(delegate);
	}
	
	/**
	 * Cache instances under keys specified by the provided mapper functions.
	 * If entity {@code e} translates to {@code m}, then {@code m} is cached under
	 * {@code entityKeyMapper.apply(e)}.
	 * In particular, if two entities {@code e1} and {@code e2} map to the same key {@code k}},
	 * the same cache entry will be used for both.
	 * The direction from MDLs to entities is analog.
	 */
	public CachingTranslator<E, MDL> usingKeys(Function<E, Object> entityKeyMapper, Function<MDL, Object> mdlKeyMapper) {
		this.entityKeyMapper = entityKeyMapper;
		this.mdlKeyMapper = mdlKeyMapper;
		clearCache();
		return this;
	}
	
	/**
	 * Specifies whether the caches in the two direction should be synchronized.
	 * If true, it is ensured that {@code toMDL(e) == mdl} iff {@code toEnttity(mdl) == e}. 
	 */
	public CachingTranslator<E, MDL> cacheInverseDirection(boolean cacheInverseDirection) {
		this.cacheInverseDirection = cacheInverseDirection;
		return this;
	}
	
	/**
	 * Prevents creation of further instances during translation.
	 * Translation returns cached instances if available, or null otherwise. 
	 */
	public CachingTranslator<E, MDL> seal(boolean sealed) {
		this.sealed = sealed;
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
	
	/**
	 * Helper method for translations.
	 * Looks up the given {@code input} in the {@code cache} using {@code keyMapper}.
	 * If the resulting key is found, the cached instance is returned.
	 * Otherwise, if sealed, null is returned.
	 * Otherwise, the given {@code delegateMethod} is called, the result is cached under the
	 * obtained key and then returned.
	 */
	private <T, R> R convertAndCache(T input, ConcurrentHashMap<Object, R> cache, Function<T, Object> keyMapper, Function<T, R> delegateMethod) {
		Object key = keyMapper.apply(input);
		// Directly use the cache if sealed instead of passing null to computeIfAbsent.
		return sealed ? cache.get(key) : cache.computeIfAbsent(key, k -> delegateMethod.apply(input));
	}
	
	/** Clear cache. */
	public void clearCache() {
		entitiesToMDL.clear();
		mdlToEntities.clear();
	}

	@Override
	public E createEntity() {
		return delegate.createEntity();
	}

	@Override
	public MDL createMDL() {
		return delegate.createMDL();
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
