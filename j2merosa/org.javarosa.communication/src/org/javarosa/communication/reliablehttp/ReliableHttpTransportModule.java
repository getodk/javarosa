package org.javarosa.communication.reliablehttp;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;

/*
 * ReliableHttp is a proprietary protocol built on top of HTTP 1.0
 * to handle retransmission of interrupted data in challenged network environments.
 * While it can be used with any server, it will only add value if communicating
 * with a reliable http server. 
 * 
 * @author <a href="mailto:rowenaluk@gmail.com">Rowena Luk</a>
 */
public class ReliableHttpTransportModule implements IModule {

	public void registerModule(Context context) {
		
		String[] classes = {
				"org.javarosa.communication.http.HttpTransportDestination",				
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		
		// RL - must set properties before instantiating TransportMethod
		// Properties can be modified from the 'Settings' menu on the main screen
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new ReliableHttpTransportProperties());
		ReliableHttpTransportMethod rhtm = new ReliableHttpTransportMethod();
        JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(rhtm);
		JavaRosaServiceProvider.instance().getTransportManager().setCurrentTransportMethod(rhtm.getId());
	}

}
