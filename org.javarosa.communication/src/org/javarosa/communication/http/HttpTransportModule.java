package org.javarosa.communication.http;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;

public class HttpTransportModule implements IModule {

	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new HttpTransportMethod());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new HttpTransportProperties());
	}

}
