package it.unibz.util;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class CollectionBidirection<One, OneOfMany, Many extends Collection<OneOfMany>> extends OneToManyBidirection<One, OneOfMany, Many> {
	
	public CollectionBidirection(
		Function<One, Many> getMany,
		BiConsumer<One, Many> setMany,
		Function<OneOfMany, One> getOne,
		BiConsumer<OneOfMany, One> setOne)
	{
		super(getMany, setMany, getOne, setOne);
	}

	protected Stream<OneOfMany> stream(Many many) {
		return many.stream();
	}
	
	protected void addImpl(Many many, OneOfMany oneOfMany) {
		if (many != null)
			many.add(oneOfMany);
	}
	
	@Override
	protected void removeImpl(Many many, OneOfMany oneOfMany) {
		if (many != null)
			many.remove(oneOfMany);
	}

	@Override
	protected boolean containsImpl(Many many, OneOfMany oneOfMany) {
		return many != null && many.contains(oneOfMany);
	}
	
	@Override
	protected void clear(Many many) {
		many.clear();
	}

	@Override
	protected void addAll(Many many, Many more) {
		many.addAll(more);
	}

}
