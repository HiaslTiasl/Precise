package it.unibz.util;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class OneToManyBidirection<One, OneOfMany, Many extends Collection<OneOfMany>> {
	
	private Function<One, Many> getMany;
	private BiConsumer<One, Many> setMany;

	private Function<OneOfMany, One> getOne;
	private BiConsumer<OneOfMany, One> setOne;
	
	public OneToManyBidirection(
		Function<One, Many> getMany,
		BiConsumer<One, Many> setMany,
		Function<OneOfMany, One> getOne,
		BiConsumer<OneOfMany, One> setOne)
	{
		this.getMany = getMany;
		this.setMany = setMany;
		this.getOne = getOne;
		this.setOne = setOne;
	}
	
	public void setMany(One one, Many many) {
		many.stream().forEach(item -> internalSetOne(item, one));
		setMany.accept(one, many);
	}

	public void setOne(OneOfMany oneOfMany, One one) {
		One oldOwner = getOne.apply(oneOfMany);
		if (oldOwner != null)
			getMany.apply(oldOwner).remove(oneOfMany);
		internalSetOne(oneOfMany, one, oldOwner);
		Collection<OneOfMany> coll = getMany.apply(one);
		if (!coll.contains(oneOfMany))
			coll.add(oneOfMany);
	}
	
	public void addOneOfMany(One one, OneOfMany oneOfMany) {
		internalSetOne(oneOfMany, one);
		getMany.apply(one).add(oneOfMany);
	}
	
	private void internalSetOne(OneOfMany oneOfMany, One one) {
		internalSetOne(oneOfMany, one, getOne.apply(oneOfMany));
	}
	
	private void internalSetOne(OneOfMany oneOfMany, One one, One oldOne) {
		if (oldOne != one)
			setOne.accept(oneOfMany, one);
	}
	
}
