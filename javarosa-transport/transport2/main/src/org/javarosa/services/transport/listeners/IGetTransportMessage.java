package org.javarosa.services.transport.listeners;


public interface IGetTransportMessage {
	
	public int getStatus();
	
	public void setStatus(int status);
	
	public long getDownloadedDate();
	
	public void setDownloadedDate(long date);
	
	public String getContentType();
	
	public void setContentType(String contentType);
	
	public Object getReturnedContent();
	
	public void setReturnedContent(Object returnedContent);
	
	public boolean isSuccess();
	
	public String getFailureReason();
	
	public void setFailureReason(String reason);
}
