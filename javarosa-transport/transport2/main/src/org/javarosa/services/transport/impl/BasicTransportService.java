package org.javarosa.services.transport.impl;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.http.HttpTransportMessage;
import org.javarosa.services.transport.impl.http.SimpleHttpTransporter;

public class BasicTransportService implements TransportService {
	
	

	private static TransportQueue queue = new TransportQueue(false);
	
	
	

	public String send(TransportMessage message) throws IOException {

		if (message.getTransportMethod() == TransportMessage.TRANSPORT_METHOD_HTTP) {
			String id = queue.enqueue(message);
			SimpleHttpTransporter httpTransporter = new SimpleHttpTransporter(
					(HttpTransportMessage) message);
			SenderThread thread = new SenderThread(httpTransporter, queue);
			thread.start();
			return id;
		}
		throw new RuntimeException("Unrecognised message type: "
				+ message.getTransportMethod());
	}

	public Vector getTransportQueue() {
		return queue.getTransportQueue();
	}

	public int getTransportQueueSize() {
		return queue.getTransportQueueSize();
	}

}
