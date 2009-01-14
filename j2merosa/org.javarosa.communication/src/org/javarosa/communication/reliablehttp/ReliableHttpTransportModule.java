package org.javarosa.communication.reliablehttp;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;

public class ReliableHttpTransportModule implements IModule {

	public void registerModule(Context context) {
		
		String[] classes = {
				"org.javarosa.communication.reliablehttp.ReliableHttpTransportDestination",				
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new ReliableHttpTransportMethod());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new ReliableHttpTransportProperties());
		JavaRosaServiceProvider.instance().getTransportManager().setCurrentTransportMethod(new ReliableHttpTransportMethod().getId());
	}

}
