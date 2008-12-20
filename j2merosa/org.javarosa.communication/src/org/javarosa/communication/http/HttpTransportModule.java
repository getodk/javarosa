package org.javarosa.communication.http;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;

public class HttpTransportModule implements IModule {

	public void registerModule(Context context) {
		
		String[] classes = {
				"org.javarosa.communication.http.HttpTransportDestination",
				"org.javarosa.communication.http.HttpTransportHeader"
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new HttpTransportMethod());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new HttpTransportProperties());
		JavaRosaServiceProvider.instance().getTransportManager().setCurrentTransportMethod(new HttpTransportMethod().getId());
	}

}
