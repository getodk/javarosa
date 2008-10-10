package org.javarosa.formmanager.utility;

import org.javarosa.core.Context;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMethod;

public class TransportContext extends Context{
	
	public final static String MAIN_MENU = "main";
	public final static String MESSAGE_VIEW = "messages";
	public final static String SEND_DATA = "send";
	
	public static final String FORM_TRANSPORT_VIEW = "form-transport-view";
	
	public TransportContext(Context context) {
		super(context);
	}
	
	public String getRequestedTask() {
		if(contextObject.containsKey(FORM_TRANSPORT_VIEW)) {
			return (String)contextObject.get(FORM_TRANSPORT_VIEW);
		}
		else {
			return MAIN_MENU;
		}
	}
	
	public void setRequestedTask(String view) {
		contextObject.put(FORM_TRANSPORT_VIEW, view);
	}
	
	public void setDestination(ITransportDestination destination) {
		contextObject.put(TransportMethod.DESTINATION_KEY, destination);
	}
	
	public ITransportDestination getDestination() {
		return (ITransportDestination)contextObject.get(TransportMethod.DESTINATION_KEY);
	}
}
