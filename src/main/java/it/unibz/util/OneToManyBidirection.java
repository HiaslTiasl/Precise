package it.unibz.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class OneToManyBidirection<One, OneOfMany, Many> {
	
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
	
	public Many getMany(One one) {
		return getMany.apply(one);
	}
	
	public void setMany(One one, Many many) {
		if (one != null) {
			Many oldMany = getMany.apply(one);
			if (oldMany == null)
				setMany.accept(one, many);
			else
				clear(oldMany);
			if (many != null)
				adjustManyToOne(one, many);
		}
	}
	
	public One getOne(OneOfMany oneOfMany) {
		return getOne.apply(oneOfMany);
	}
	
	public void setOne(OneOfMany oneOfMany, One one) {
		One oldOne = getOne.apply(oneOfMany);
		if (oldOne != null)
			removeImpl(getMany.apply(oldOne), oneOfMany);
		if (one != null) {
			setOne.accept(oneOfMany, one);
			addIfAbsent(getMany.apply(one), oneOfMany);
		}
	}
	
	public void addOneOfMany(One one, OneOfMany oneOfMany) {
		setOne.accept(oneOfMany, one);
		addImpl(getMany.apply(one), oneOfMany);
	}
	
	protected void addIfAbsent(Many many, OneOfMany oneOfMany) {
		if (many != null && !containsImpl(many, oneOfMany))
			addImpl(many, oneOfMany);
	}
	
	protected abstract void removeImpl(Many many, OneOfMany oneOfMany);
	protected abstract boolean containsImpl(Many many, OneOfMany oneOfMany);
	protected abstract void addImpl(Many many, OneOfMany oneOfMany);
	protected abstract Stream<OneOfMany> stream(Many many);
	
	protected abstract void clear(Many many);
	protected abstract void addAll(Many many, Many more);
	
	protected void adjustManyToOne(One one, Many many) {
		if (many != null)
			stream(many).forEach(oneOfMany -> setOne(oneOfMany, one));
	}
	
}
