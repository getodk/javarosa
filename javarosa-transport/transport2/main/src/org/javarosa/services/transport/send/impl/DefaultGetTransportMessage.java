package org.javarosa.services.transport.send.impl;

import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.listeners.IGetTransportMessage;

public abstract class DefaultGetTransportMessage implements IGetTransportMessage {

	private int status;
	private long downloadedDate;
	private Object returnedContent;
	private String contentType;
	private String downloadFailureReason;
	
	
	public int getStatus(){
		return this.status;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public long getDownloadedDate(){
		return this.downloadedDate;
	}
	
	public void setDownloadedDate(long date){
		this.downloadedDate = date;
	}
	
	public String getContentType(){
		return this.contentType;
	}
	
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	
	public Object getReturnedContent(){
		return this.returnedContent;
	}
	
	public void setReturnedContent(Object returnedContent){
		this.returnedContent = returnedContent;
	}
	
	public boolean isSuccess(){
		return this.status == TransportMessageStatus.DOWNLOADED;
	}
	
	public String getFailureReason(){
		return this.downloadFailureReason;
	}
	
	public void setFailureReason(String reason){
		this.downloadFailureReason = reason;
	}
}
