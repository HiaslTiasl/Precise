package it.unibz.precise.model;

/**
 * Base class for commonly referring to custom exceptions.
 * 
 * @author MatthiasP
 *
 */
public abstract class PreciseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PreciseException() {
		super();
	}

	public PreciseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PreciseException(String message, Throwable cause) {
		super(message, cause);
	}

	public PreciseException(String message) {
		super(message);
	}

	public PreciseException(Throwable cause) {
		super(cause);
	}

	
	
}
