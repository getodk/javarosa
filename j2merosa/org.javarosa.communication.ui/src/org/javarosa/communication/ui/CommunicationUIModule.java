package org.javarosa.communication.ui;

import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.http.ui.HttpDestinationRetrievalActivity;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class CommunicationUIModule implements IModule {

	public void registerModule(Context context) {
		HttpTransportMethod http = (HttpTransportMethod)JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(new HttpTransportMethod().getId());
		http.setDestinationRetrievalActivity(new HttpDestinationRetrievalActivity());
	}

}
