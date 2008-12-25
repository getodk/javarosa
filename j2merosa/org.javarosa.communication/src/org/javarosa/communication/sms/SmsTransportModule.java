package org.javarosa.communication.sms;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class SmsTransportModule implements IModule{
	public void registerModule(Context context) {
		
		String[] classes = {
				"org.javarosa.communication.sms.SmsTransportDestination",				
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new SmsTransportMethod());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new SmsTransportProperties());
		JavaRosaServiceProvider.instance().getTransportManager().setCurrentTransportMethod(new SmsTransportMethod().getId());
	}

}
