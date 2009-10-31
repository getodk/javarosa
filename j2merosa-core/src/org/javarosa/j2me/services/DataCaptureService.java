package org.javarosa.j2me.services;

public interface DataCaptureService {
	String IMAGE = "image";
	String AUDIO = "audio";
	String VIDEO = "video";
	String BARCODE = "barcode";
	String LOCATION = "loc";
	String RFID = "rfid";
	String SMELLOVISION = "scent";

	String getType ();
	
}
