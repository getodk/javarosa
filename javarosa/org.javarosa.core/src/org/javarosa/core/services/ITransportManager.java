/*
 * Copyright (C) 2009 JavaRosa-Core Project
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
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.Observable;

/**
 * An ITransportManager is responsible for transmitting byte
 * data using various Transport Methods.
 * 
 * @author 
 *
 */
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
	public abstract void enqueue(IDataPayload data, ITransportDestination destination,
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
	 * Sets the current transport method.
	 * @param transportMethodId The Id of a registered transport method.
	 * The current transport method will only be set if this Id is registered.
	 */
	public abstract void setCurrentTransportMethod(int transportMethodId);
	
	public abstract int getCurrentTransportMethod();
	
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
	public Vector getModelDeliveryStatuses (Vector modelIDs, boolean notFoundOK);
	
	public void markSent (int modelID, boolean checkQueue);

		/**
	 * 
	 * @param i The index of the transport method whose current default destination
	 * should be returned.
	 * @return The default Transport Destination current used by the transport
	 * method requested.
	 */
	public ITransportDestination getDefaultTransportDestination(int transportMethod);
}