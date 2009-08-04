package org.javarosa.services.transport;

import org.javarosa.services.transport.message.TransportMessage;

public interface TransportMessageFactory {
	public TransportMessage getMessage();
}
