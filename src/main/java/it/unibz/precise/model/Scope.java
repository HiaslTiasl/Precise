package it.unibz.precise.model;

/**
 * Scope.
 *
 * @author Matthias Perktold
 * @since  2016-ToDo (MP)
 */
public enum Scope {

	UNIT(0), SECTION(1), LEVEL(2), SECTOR(3), TASK(4);

	private int depth;

	private Scope(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}
}
