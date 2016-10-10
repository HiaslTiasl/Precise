package it.unibz.precise.rest;

public class FileDownload {
	
	public static String getContentDisposition(String filename) {
		return "attachment; filename=" + filename;
	}

}
