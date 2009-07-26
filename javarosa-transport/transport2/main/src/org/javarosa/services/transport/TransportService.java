package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Vector;

public interface TransportService {

	/**
	 * @param message
	 * @throws IOException
	 */
	public void send(TransportMessage message) throws IOException;

	/**
	 * @return
	 */
	public Vector getTransportQueue();

	/**
	 * @return
	 */
	public int getTransportQueueSize();

}
