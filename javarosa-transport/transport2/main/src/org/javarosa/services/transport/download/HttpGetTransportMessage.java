package org.javarosa.services.transport.download;

import java.util.Date;

import org.javarosa.services.transport.listeners.IGetTransporter;
import org.javarosa.services.transport.send.impl.DefaultGetTransportMessage;

import de.enough.polish.io.Serializable;

public class HttpGetTransportMessage extends DefaultGetTransportMessage implements Serializable {

	private byte [] data;
	private String destinationURL;
	private String downloadFailureReason;
	
	public HttpGetTransportMessage(String destinationURL){
		this.destinationURL = destinationURL;
	}
	
	public String geDestinationURL(){
		return this.destinationURL;
	}
	
	public void setDestinationURL(String destinationURL){
		this.destinationURL = destinationURL;
	}
	
	public IGetTransporter createGetTransporter() {
		return new HttpGetTransporter();
	}
	
	public Object getReturnedContent(){
		return this.data;
	}
	
	public void setReturnedContent(byte[] data){
		this.data = data;
	}

	public long getDownloadedDate() {
		Date downloadedDate = new Date();
		
		return downloadedDate.getTime();
	}

	public String getFailureReason() {
		return this.downloadFailureReason;
	}

	public void setFailureReason(String reason) {
		this.downloadFailureReason = reason;
		
	}
}
