package org.javarosa.core.services.transport;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Storage types for Transport Messages
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public interface Storage {

	/**
	 * Stores transport message locally
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void saveMessage(TransportMessage message) throws IOException;

	/**
	 * Updates a saved message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void updateMessage(TransportMessage message) throws IOException;
	
	/**
	 * Deletes a saved message 
	 * @param msgIndex The index of the message to be removed from local storage
	 * @throws IOException
	 */
	public void deleteMessage(int msgIndex) throws IOException;
	
	/**
	 * Closes the local storage element 
	 */
	public void close();

	/**
	 * Get the messages stored locally
	 * 
	 * @return An enumeration of the locally stored messages
	 */
	public Enumeration messageElements();

}
