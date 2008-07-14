package org.javarosa.core.services;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.Storage;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.Observable;
import org.javarosa.core.util.Observer;

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

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.ITransportManager#enqueue(byte[], java.lang.String, int, int)
	 */
	public void enqueue(byte[] data, String destination, int transportMethod, int formDataId)
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
	private void enqueue(TransportMessage message, int transportMethod)
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
			storage.updateMessage((TransportMessage) observable);
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

}
