package it.unibz.precise.model;

/**
 * Scope.
 *
 * @author Matthias Perktold
 * @since  2016-ToDo (MP)
 */
public enum Scope {

	TASK(4), SECTOR(3), LEVEL(2), SECTION(1), UNIT(0);

	private int depth;

	private Scope(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}
}
