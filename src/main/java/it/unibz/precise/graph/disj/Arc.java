package it.unibz.precise.graph.disj;

/**
 * Represents a precedence relation from a source node to a target node.
 * 
 * @author MatthiasP
 *
 * @param <T> The type of the nodes
 */
public class Arc<T> {
	
	private T source;
	private T target;

	public Arc(T source, T target) {
		this.source = source;
		this.target = target;
	}

	public T getSource() {
		return source;
	}

	public T getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Arc<?> other = (Arc<?>) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return source + " --> " + target;
	}

}
