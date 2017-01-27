package it.unibz.util;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link OneToManyBidirection} where the many site is a map such that the key can be computed,
 * out of the value, i.e. out of elements of the many site.
 * 
 * @author MatthiasP
 *
 * @param <Key>
 */
public class MapBidirection<Key, One, OneOfMany, Many extends Map<Key, OneOfMany>> extends OneToManyBidirection<One, OneOfMany, Many> {
	
	/** Returns the key for a map value (i.e. an element of the many side). */
	private Function<OneOfMany, Key> getKey;
	
	public MapBidirection(
		Function<One, Many> getMany,
		BiConsumer<One, Many> setMany,
		Function<OneOfMany, One> getOne,
		BiConsumer<OneOfMany, One> setOne,
		Function<OneOfMany, Key> getKey)
	{
		super(getMany, setMany, getOne, setOne);
		this.getKey = getKey;
	}
	
	@Override
	protected boolean contains(Many many, OneOfMany oneOfMany) {
		return many != null && many.containsKey(getKey.apply(oneOfMany));
	}
	
	@Override
	protected void clear(Many many) {
		many.clear();
	}
	
	@Override
	protected void remove(Many many, OneOfMany oneOfMany) {
		if (many != null)
			many.remove(getKey.apply(oneOfMany), oneOfMany);
	}
	
	protected void add(Many many, OneOfMany oneOfMany) {
		if (many != null)
			many.put(getKey.apply(oneOfMany), oneOfMany);
	}

	@Override
	protected void addAll(Many many, Many more) {
		many.putAll(more);
	}
	
	protected Stream<OneOfMany> stream(Many many) {
		return many.values().stream();
	}

}

