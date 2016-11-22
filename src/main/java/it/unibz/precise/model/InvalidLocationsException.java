package it.unibz.precise.model;

public class InvalidLocationsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidLocationsException(Task task, String message) {
		super(task.getShortIdentification() + ": " + message);
	}
	
}
