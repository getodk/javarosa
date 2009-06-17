/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.services;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.log.IncidentLog;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.Storage;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.Observable;
import org.javarosa.core.util.Observer;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * The transport manager is responsible for the registration of different
 * transport methods, which deal with receiving and transmitting data
 * from various sources.
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class TransportManager implements Observer, IService, ITransportManager {

	/**
	 * The unique identifier of the device. Hard coded for now.
	 */
	
	private static final String ID = "TestDevice";

	/**
	 * Holds available transport methods. We chose a Hashtable instead of a Map,
	 * because it is available in CLDC as well.
	 */
	private Hashtable transportMethods = new Hashtable();

	/**
	 * Listens for messages from different transport methods
	 */
	private MessageListener messageListener;

	/**
	 * A storage element for placing received messages
	 */
	private Storage storage;
	
	/**
	 * The current transport method that has been set for use
	 */
	private int currentTransportMethod = -1;
	
	/**
	 * Creates a new instance of <code>TransportManager</code>
	 *
	 * @param storage
	 */
	public TransportManager(Storage storage) {
		if (storage == null) {
			throw new IllegalArgumentException(
					"Parameter storage must not be null");
		}
		this.storage = storage;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#getName()
	 */
	public String getName() {
		return "Transport Manager";
	}
	
	/**
	 * Sets the current transport method.
	 * @param transportMethodId The Id of a registered transport method.
	 * The current transport method will only be set if this Id is registered.
	 */
	public void setCurrentTransportMethod(int transportMethodId) {
		Integer method = new Integer(transportMethodId);
		Enumeration en = transportMethods.keys();
		while(en.hasMoreElements()) {
			if(method.equals(en.nextElement())){
				this.currentTransportMethod = transportMethodId;
			}
		}
	}
	
	public int getCurrentTransportMethod() {
		return this.currentTransportMethod;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#enqueue(byte[], java.lang.String, int, int)
	 */
	public void enqueue(IDataPayload data, ITransportDestination destination, int transportMethod, int formDataId)
			throws IOException {
		TransportMessage message = new TransportMessage(data, destination, ID, formDataId);
		enqueue(message, transportMethod);
	}

	/**
	 * Enqueues a TransportMessage to be transmitted using the given transport method.
	 * 
	 * @param message The message to be sent
	 * @param transportMethod the ID of a TransportMethod registered with this manager
	 * @throws IOException If the transport method requested is not available 
	 */
	public void enqueue(TransportMessage message, int transportMethod)
			throws IOException {
		message.addObserver(this);
		TransportMethod selectedMethod = getTransportMethod(transportMethod);
		storage.saveMessage(message);
		if (selectedMethod == null) {
			throw new IOException("Selected transport method not available");
		}
		selectedMethod.transmit(message, this);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#getTransportMethod(int)
	 */
	public TransportMethod getTransportMethod(int transportMethod) {
		return (TransportMethod) transportMethods.get(new Integer(
				transportMethod));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#showMessage(java.lang.String, int)
	 */
	public void showMessage(String string, int messageType) {
		if (messageListener != null) {
			messageListener.onMessage(string, messageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#setMessageListener(org.javarosa.core.services.transport.MessageListener)
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	/**
	 * Starts a Bluetooth server on the current device
	 */
	public void startBluetoothServer() {
		// new BluetoothTransportMethod().startServer(this);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#send(org.javarosa.core.services.transport.TransportMessage, int)
	 */
	public void send(TransportMessage message, int transportMethod) {
		TransportMethod method = getTransportMethod(transportMethod);
		message.addObserver(this);
		method.transmit(message, this);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#registerTransportMethod(org.javarosa.core.services.transport.TransportMethod)
	 */
	public void registerTransportMethod(TransportMethod transportMethod) {
		transportMethods.put(new Integer(transportMethod.getId()),
				transportMethod);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#deregisterTransportMethod(org.javarosa.core.services.transport.TransportMethod)
	 */
	public void deregisterTransportMethod(TransportMethod transportMethod) {
		transportMethods.remove(new Integer(transportMethod.getId()));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#getTransportMethods()
	 */
	public Enumeration getTransportMethods() {
		return transportMethods.elements();
	}

	/**
	 *
	 */
	public void cleanUp() {
		storage.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.transport.Observer#update(org.openmrs.transport.Observable,
	 *      java.lang.Object)
	 */
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#update(org.javarosa.core.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object arg) {
		try {
			TransportMessage msg = (TransportMessage)observable;
			storage.updateMessage(msg);
			if(msg != null && msg.getStatus() == TransportMessage.STATUS_FAILED) {
				JavaRosaServiceProvider.instance().logIncident(IncidentLog.LOG_TYPE_APPLICATION, "Attempted Message Send Failure!");
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.transport.Observer#update(org.openmrs.transport.Observable,
	 *      java.lang.Object)
	 */
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#deleteMessage(int)
	 */
	public void deleteMessage(int msgIndex) {
		try {
			storage.deleteMessage(msgIndex);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#getMessages()
	 */
	public Enumeration getMessages() {
		return storage.messageElements();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#updateMessage(org.javarosa.core.services.transport.TransportMessage)
	 */
	public void updateMessage(TransportMessage message) throws IOException {
		storage.updateMessage(message);
	}

	public int getModelDeliveryStatus(int modelId, boolean notFoundOK) {
		Vector v = new Vector();
		v.addElement(new Integer(modelId));
		return ((Integer)getModelDeliveryStatuses(v, notFoundOK).elementAt(0)).intValue();
	}
	
	public Vector getModelDeliveryStatuses (Vector modelIDs, boolean notFoundOK) {
		Hashtable statuses = new Hashtable();
		Hashtable times = new Hashtable();
		
		//TODO: Are we OK with using the transport manager here? There's coupling...
		//TODO: The way we're doing this is fairly wasteful. We should store them
		//locally, and update on change, instead of getting each one.
		Enumeration qMessages = getMessages();
		while(qMessages.hasMoreElements()) {
			TransportMessage message = (TransportMessage)qMessages.nextElement();
			
			int modelID = message.getModelId();
			int status = message.getStatus();
			
			if (modelIDs.contains(new Integer(modelID))) {
				boolean replace = true;
				if(statuses.containsKey(new Integer(modelID))) {
					Date oldMessage = (Date)times.get(new Integer(modelID));
					if(oldMessage.getTime() > message.getTimestamp().getTime()) {
						replace = false;
					}
				}
				if (replace) {
					statuses.put(new Integer(modelID), new Integer(status));
					times.put(new Integer(modelID), message.getTimestamp());
				}
			}
		}
		
		Vector statusV = new Vector();
		for (int i = 0; i < modelIDs.size(); i++) {
			Integer modelID = (Integer)modelIDs.elementAt(i);
			int status;
			if (statuses.containsKey(modelID)) {
				status = ((Integer)statuses.get(modelID)).intValue();
			} else {
				status = (notFoundOK ? TransportMessage.STATUS_NOT_SENT : -1);
			}
			
			statusV.addElement(new Integer(status));
		}
		return statusV;
	}
	
	public void markSent (int modelID, boolean checkQueue) {
		TransportMessage message = null;
		boolean newMessage;

		if (checkQueue) {
			for (Enumeration e = getMessages(); e.hasMoreElements(); ) {
				TransportMessage tm = (TransportMessage)e.nextElement();
				if (tm.getModelId() == modelID) {
					message = tm;
					break;
				}
			}
		}
		
		newMessage = (message == null);
		if (newMessage) {
			ITransportDestination nulldest = null;
			try {
				nulldest = (ITransportDestination)PrototypeFactory.getInstance(Class.forName("org.javarosa.communication.http.HttpTransportDestination"));
			} catch (ClassNotFoundException e) { }
			message = new TransportMessage(null, nulldest, "", modelID);
		}
		
		if (message.getStatus() != TransportMessage.STATUS_DELIVERED) {
			message.setStatus(TransportMessage.STATUS_DELIVERED);
		
			try {
				if (newMessage) {
					storage.saveMessage(message);
				} else {
					storage.updateMessage(message);
				}
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * 
	 * @param i The index of the transport method whose current default destination
	 * should be returned.
	 * @return The default Transport Destination current used by the transport
	 * method requested.
	 */
	public ITransportDestination getDefaultTransportDestination(int i) {
		return ((TransportMethod)transportMethods.get(new Integer(i))).getDefaultDestination();
	}
	
	public void wipeAssociatedMessages(Vector modelIDs) {
		Stack toClear = new Stack();
		
		Enumeration qMessages = getMessages();
		while(qMessages.hasMoreElements()) {
			TransportMessage message = (TransportMessage)qMessages.nextElement();
			
			int modelID = message.getModelId();
			if(modelIDs.contains(new Integer(modelID))) {
				toClear.addElement(new Integer(message.getRecordId()));
			}
		}
		
		while(!toClear.isEmpty()) {
			Integer mid = (Integer)toClear.pop();
			try {
				storage.deleteMessage(mid.intValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
