package it.unibz.precise.model;

public class InvalidTaskException extends PreciseException {

	private static final long serialVersionUID = 1L;

	public InvalidTaskException(Task task, String message) {
		super(task.getShortIdentification() + ": " + message);
	}
	
}
