package it.unibz.util;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapBidirection<Key, One, OneOfMany, Many extends Map<Key, OneOfMany>> extends OneToManyBidirection<One, OneOfMany, Many> {
	
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
	
	protected Stream<OneOfMany> stream(Many many) {
		return many.values().stream();
	}
	
	protected void addImpl(Many many, OneOfMany oneOfMany) {
		if (many != null)
			many.put(getKey.apply(oneOfMany), oneOfMany);
	}
	
	@Override
	protected void removeImpl(Many many, OneOfMany oneOfMany) {
		if (many != null)
			many.remove(getKey.apply(oneOfMany), oneOfMany);
	}

	@Override
	protected boolean containsImpl(Many many, OneOfMany oneOfMany) {
		return many != null && many.containsKey(getKey.apply(oneOfMany));
	}

}
