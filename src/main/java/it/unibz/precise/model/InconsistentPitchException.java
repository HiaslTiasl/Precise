package it.unibz.precise.model;

public class InconsistentPitchException extends PreciseException {

	private static final long serialVersionUID = 1L;

	public InconsistentPitchException() {
		super("pitch parameter values are inconsistent");
	}

}
