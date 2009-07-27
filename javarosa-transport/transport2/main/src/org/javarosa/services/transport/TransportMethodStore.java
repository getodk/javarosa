package org.javarosa.services.transport;

import java.util.Hashtable;

public class TransportMethodStore {
	private Hashtable methodStore = new Hashtable();
	
	public TransportMethodStore(){
		
	}
	
	public void register(ITransportMethod transportMethod){
		methodStore.put(transportMethod.getClass().getName(), transportMethod);
	}
	public void deregister(ITransportMethod transportMethod){
		methodStore.remove(transportMethod.getClass().getName());
	}
}
