package org.javarosa.core.services;

import java.io.IOException;
import java.util.Enumeration;

import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.Observable;

public interface ITransportManager extends IService {

	public abstract String getName();

	/**
	 * Enqueues a block of data to be transmitted using the given transport method.
	 * 
	 * @param data The block of data to be transmitted
	 * @param destination The destination for the given data
	 * @param transportMethod the ID of a TransportMethod registered with this manager
	 * @param formDataId The ID of the form that should be transmitted
	 * @throws IOException If the transport method requested is not available 
	 */
	public abstract void enqueue(byte[] data, String destination,
			int transportMethod, int formDataId) throws IOException;

	/**
	 * Returns a TransportMethod registered with this Manager
	 * 
	 * @param transportMethod The ID of the transport method being
	 * requested
	 * 
	 * @return A TransportMethod corresponding to the given 
	 * ID, registered with this manager. null if none exists 
	 */
	public abstract TransportMethod getTransportMethod(int transportMethod);

	/**
	 * Fires a string to the current message listener
	 * 
	 * @param string The message to be shown
	 * @param messageType The type of the given message
	 */
	public abstract void showMessage(String string, int messageType);

	/**
	 * Sets a new message listener for this manager.
	 * 
	 * @param messageListener
	 *            the messageListener to set
	 */
	public abstract void setMessageListener(MessageListener messageListener);

	/**
	 * Sends a message using the given transport method
	 * 
	 * @param message The message to be transmitted
	 * @param transportMethod the ID of a TransportMethod registered
	 * with this manager to be used to transmit the given message
	 */
	public abstract void send(TransportMessage message, int transportMethod);

	/**
	 * Registers a new TransportMethod with this manager, able to be retrieved
	 * by the method's Id.
	 * 
	 * @param transportMethod The method to be registered
	 */
	public abstract void registerTransportMethod(TransportMethod transportMethod);

	/**
	 * Removes the given TransportMethod from this manager, if it is currently
	 * registered.
	 * 
	 * @param transportMethod The method to be removed from the manager
	 */
	public abstract void deregisterTransportMethod(
			TransportMethod transportMethod);

	/**
	 * Returns an enumeration of all of the TransportMethods currently
	 * available in the manager.
	 * 
	 * @return an Enumeration of all available transport methods
	 */
	public abstract Enumeration getTransportMethods();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.transport.Observer#update(org.openmrs.transport.Observable,
	 *      java.lang.Object)
	 */
	public abstract void update(Observable observable, Object arg);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.transport.Observer#update(org.openmrs.transport.Observable,
	 *      java.lang.Object)
	 */
	public abstract void deleteMessage(int msgIndex);

	/**
	 * Returns all of the current messages received by the TransportManager
	 * 
	 * @return An enumeration of all messages received by the manager
	 */
	public abstract Enumeration getMessages();

	/**
	 * Updates a given message stored in the manager
	 * 
	 * @param message The message to be updated
	 * @throws IOException
	 */
	public abstract void updateMessage(TransportMessage message)
			throws IOException;

	
	public int getModelDeliveryStatus (int modelID, boolean notFoundOK);
}