package org.javarosa.communication.file;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;

public class FileTransportModule implements IModule {

	public void registerModule(Context context) {
		
		String[] classes = {
				"org.javarosa.communication.file.FileTransportDestination",				
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new FileConnectionTransportMethod());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new FileTransportProperties());
	}

}
