package org.javarosa.core.services.transport;

import java.io.IOException;
import java.util.Enumeration;

/**
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
	 * @param message
	 * @throws IOException
	 */
	public void updateMessage(TransportMessage message) throws IOException;
	
	/**
	 * @param message
	 * @throws IOException
	 */
	public void deleteMessage(int msgIndex) throws IOException;
	/**
	 * 
	 */
	public void close();

	/**
	 * @return
	 */
	public Enumeration messageElements();

}
