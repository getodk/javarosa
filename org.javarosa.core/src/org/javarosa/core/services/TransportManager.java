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
	
	public String getName() {
		return "Transport Manager";
	}

	/**
	 * Enqueues a block of data to be transmitted using the given transport method.
	 * 
	 * @param data The block of data to be transmitted
	 * @param destination The destination for the given data
	 * @param transportMethod the ID of a TransportMethod registered with this manager
	 * @param formDataId The ID of the form that should be transmitted
	 * @throws IOException If the transport method requested is not available 
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
	 * Returns a TransportMethod registered with this Manager
	 * 
	 * @param transportMethod The ID of the transport method being
	 * requested
	 * 
	 * @return A TransportMethod corresponding to the given 
	 * ID, registered with this manager. null if none exists 
	 */
	public TransportMethod getTransportMethod(int transportMethod) {
		return (TransportMethod) transportMethods.get(new Integer(
				transportMethod));
	}

	/**
	 * Fires a string to the current message listener
	 * 
	 * @param string The message to be shown
	 * @param messageType The type of the given message
	 */
	public void showMessage(String string, int messageType) {
		if (messageListener != null) {
			messageListener.onMessage(string, messageType);
		}
	}

	/**
	 * Sets a new message listener for this manager.
	 * 
	 * @param messageListener
	 *            the messageListener to set
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

	/**
	 * Sends a message using the given transport method
	 * 
	 * @param message The message to be transmitted
	 * @param transportMethod the ID of a TransportMethod registered
	 * with this manager to be used to transmit the given message
	 */
	public void send(TransportMessage message, int transportMethod) {
		TransportMethod method = getTransportMethod(transportMethod);
		message.addObserver(this);
		method.transmit(message, this);
	}

	/**
	 * Registers a new TransportMethod with this manager, able to be retrieved
	 * by the method's Id.
	 * 
	 * @param transportMethod The method to be registered
	 */
	public void registerTransportMethod(TransportMethod transportMethod) {
		transportMethods.put(new Integer(transportMethod.getId()),
				transportMethod);
	}

	/**
	 * Removes the given TransportMethod from this manager, if it is currently
	 * registered.
	 * 
	 * @param transportMethod The method to be removed from the manager
	 */
	public void deregisterTransportMethod(TransportMethod transportMethod) {
		transportMethods.remove(new Integer(transportMethod.getId()));
	}

	/**
	 * Returns an enumeration of all of the TransportMethods currently
	 * available in the manager.
	 * 
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
	 * Returns all of the current messages received by the TransportManager
	 * 
	 * @return An enumeration of all messages received by the manager
	 */
	public Enumeration getMessages() {
		return storage.messageElements();
	}

	/**
	 * Updates a given message stored in the manager
	 * 
	 * @param message The message to be updated
	 * @throws IOException
	 */
	public void updateMessage(TransportMessage message) throws IOException {
		storage.updateMessage(message);
	}

}
