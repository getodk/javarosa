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
 *
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class TransportManager implements Observer, IService {

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
	 *
	 */
	private MessageListener messageListener;

	/**
	 *
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
	
	public String getName() {
		return "Transport Manager";
	}

	/**
	 * @param data
	 * @param destination
	 * @param transportMethod
	 * @throws IOException
	 */
	public void enqueue(byte[] data, String destination, int transportMethod, int modelId)
			throws IOException {
		TransportMessage message = new TransportMessage(data, destination, ID, modelId);
		enqueue(message, transportMethod);
	}

	/**
	 * @param message
	 * @param transportMethod
	 * @throws IOException
	 */
	private void enqueue(TransportMessage message, int transportMethod)
			throws IOException {
		System.out.println("Enqueue transport message with status "
				+ message.statusToString());
		message.addObserver(this);
		TransportMethod selectedMethod = getTransportMethod(transportMethod);
		storage.saveMessage(message);
		if (selectedMethod == null) {
			throw new IOException("Selected transport method not available");
		}
		selectedMethod.transmit(message, this);
	}

	/**
	 *
	 */
	public TransportMethod getTransportMethod(int transportMethod) {
		return (TransportMethod) transportMethods.get(new Integer(
				transportMethod));
	}

	public void showMessage(String string, int messageType) {
		if (messageListener != null) {
			messageListener.onMessage(string, messageType);
		}
	}

	/**
	 * @param messageListener
	 *            the messageListener to set
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public void startBluetoothServer() {
		// new BluetoothTransportMethod().startServer(this);
	}

	/**
	 * @param message
	 * @param http
	 */
	public void send(TransportMessage message, int transportMethod) {
		TransportMethod method = getTransportMethod(transportMethod);
		message.addObserver(this);
		method.transmit(message, this);
	}

	/**
	 * @param transportMethod
	 */
	public void registerTransportMethod(TransportMethod transportMethod) {
		transportMethods.put(new Integer(transportMethod.getId()),
				transportMethod);
	}

	/**
	 * @param transportMethod
	 */
	public void deregisterTransportMethod(TransportMethod transportMethod) {
		transportMethods.remove(new Integer(transportMethod.getId()));
	}

	/**
	 * @return an Enumeration of all available transport methods
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
	public void deleteMessage(int msgIndex) {
		try {
			storage.deleteMessage(msgIndex);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * @return
	 */
	public Enumeration getMessages() {
		return storage.messageElements();
	}

	/**
	 * @param message
	 * @throws IOException
	 */
	public void updateMessage(TransportMessage message) throws IOException {
		storage.updateMessage(message);
	}

}
