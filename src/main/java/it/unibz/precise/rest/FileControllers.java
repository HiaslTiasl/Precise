package it.unibz.precise.rest;

/**
 * Static constants and helper methods for controllers exposing models
 * as virtual files.
 * 
 * @author MatthiasP
 *
 */
public class FileControllers {

	public static final String ROOT_PATH = "/files";
	public static final String NAME_PATTERN = "/{name}";
	
	/** Returns the "Content-Disposition" HTTP Header value for the given filename. */
	public static String getContentDisposition(String filename, String ext) {
		return "attachment; filename=\"" + filename + "\"";
	}

}
