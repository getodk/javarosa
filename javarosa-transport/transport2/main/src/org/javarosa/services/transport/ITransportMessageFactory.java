package org.javarosa.services.transport;

import org.javarosa.services.transport.message.TransportMessage;

public interface ITransportMessageFactory {
	public TransportMessage getMessage();
}
