package org.javarosa.communication.ui;

import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.http.ui.HttpDestinationRetrievalActivity;
import org.javarosa.communication.sms.SmsTransportMethod;
import org.javarosa.communication.sms.ui.SmsDestinationRetrievalActivity;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class CommunicationUIModule implements IModule {

	public void registerModule(Context context) {
		HttpTransportMethod http = (HttpTransportMethod)JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(new HttpTransportMethod().getId());
		if(http != null) {
			http.setDestinationRetrievalActivity(new HttpDestinationRetrievalActivity());
		}
		SmsTransportMethod sms = (SmsTransportMethod)JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(new SmsTransportMethod().getId());
		if(sms != null) {
			sms.setDestinationRetrievalActivity(new SmsDestinationRetrievalActivity());
		}
	}

}
