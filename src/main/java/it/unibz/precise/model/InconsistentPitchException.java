package it.unibz.precise.model;

/**
 * An exception for inconsistent pitch parameter in tasks.
 * 
 * @author MatthiasP
 *
 */
public class InconsistentPitchException extends PreciseException {

	private static final long serialVersionUID = 1L;

	public InconsistentPitchException() {
		super("pitch parameter values are inconsistent");
	}

}
