package org.javarosa.communication.ui;

import org.javarosa.core.services.transport.ITransportDestination;

public interface GetDestinationTransitions {

	void cancel ();
	
	void entered (ITransportDestination dest);
	
}
