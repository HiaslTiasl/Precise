package it.unibz.precise.rest;

/**
 * Static methods for responses that should trigger a file download.
 * 
 * @author MatthiasP
 *
 */
public class FileDownload {
	
	/** Returns the "Content-Disposition" HTTP Header value for the given filename. */
	public static String getContentDisposition(String filename) {
		return "attachment; filename=\"" + filename + "\"";
	}

}
