package it.unibz.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.persistence.OneToMany;

/**
 * Helper class for managing bidirectional {@link OneToMany} relationships in JPA.
 * Receives getter and setter methods in the constructor and provides public set
 * and add methods that use the internal getters and setters to manage both directions.
 * 
 * @author MatthiasP
 *
 * @param <One>			The type of the one side
 * @param <OneOfMany>	The type of the many side
 * @param <Many>		The type for referring to multiples of the many side
 */
public abstract class OneToManyBidirection<One, OneOfMany, Many> {
	
	/** Returns many associated to one. */
	private Function<One, Many> getMany;
	/** Associates many to one. */
	private BiConsumer<One, Many> setMany;

	/** Returns one associated to one of many. */
	private Function<OneOfMany, One> getOne;
	/** Associates one to one of many. */
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
	
	
	/** Returns many associated to one. */
	public Many getMany(One one) {
		return getMany.apply(one);
	}
	
	/** Associates many to one in both directions. */
	public void setMany(One one, Many many) {
		if (one != null) {
			Many oldMany = getMany(one);
			// Only replace collections if original one is null,
			// to not replace JPA-provider implementations.
			if (oldMany == null)
				setMany.accept(one, many);
			else
				clear(oldMany);
			if (many != null)
				adjustManyToOne(one, many);
		}
	}
	
	/** Returns one associated to one of many. */
	public One getOne(OneOfMany oneOfMany) {
		return getOne.apply(oneOfMany);
	}
	
	/** Associates one to one of many in both directions. */
	public void setOne(OneOfMany oneOfMany, One one) {
		One oldOne = getOne(oneOfMany);
		// Reset old one if necessary
		if (oldOne != null)
			remove(getMany(oldOne), oneOfMany);
		setOne.accept(oneOfMany, one);
		// Add to one if necessary
		if (one != null)
			addIfAbsent(getMany(one), oneOfMany);
	}
	
	/**
	 * Adds {@code oneOfMany} to {@code one} and manages inverse direction.
	 * N.B: In contrast to {@link #setOne}, this method does not check whether
	 * {@code oneOfMany} is already contained in {@code one}.
	 */
	public void addOneOfMany(One one, OneOfMany oneOfMany) {
		setOne.accept(oneOfMany, one);
		Many many = getMany(one);
		add(many, oneOfMany);
	}

	/** Adds {@code oneOfMany} to {@code one} if not already contained. */
	protected void addIfAbsent(Many many, OneOfMany oneOfMany) {
		if (many != null && !contains(many, oneOfMany))
			add(many, oneOfMany);
	}
	
	/**
	 * Ensures that each element in {@code many} is associated to {@code one}
	 * in both directions.
	 */
	protected void adjustManyToOne(One one, Many many) {
		if (many != null)
			stream(many).forEach(oneOfMany -> setOne(oneOfMany, one));
	}
	
	/** Indicates whether {@code many} contains {@code oneOfMany}. */
	protected abstract boolean contains(Many many, OneOfMany oneOfMany);
	/** Clears {@code many}. */
	protected abstract void clear(Many many);
	/** Removes {@code oneOfMany} from {@code many}. */
	protected abstract void remove(Many many, OneOfMany oneOfMany);
	/** Adds {@code oneOfMany} to {@code many}. */
	protected abstract void add(Many many, OneOfMany oneOfMany);
	/** Adds all elements in {@code more} to {@code many}. */
	protected abstract void addAll(Many many, Many more);
	/** Converts {@code many} to a {@link Stream}. */
	protected abstract Stream<OneOfMany> stream(Many many);
	
}
