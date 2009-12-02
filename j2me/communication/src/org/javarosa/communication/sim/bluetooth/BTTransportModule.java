package org.javarosa.communication.sim.bluetooth;

import org.javarosa.communication.sim.bluetooth.BTTransportMethod;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;

public class BTTransportModule implements IModule{

	public void registerModule(Context context) {
		// TODO Auto-generated method stub
		String[] classes = {"org.javarosa.communication.sim.bluetooth.BTTransportDestination"};
        
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
        
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new BTTransportMethod());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new BTTransportProperties());
		JavaRosaServiceProvider.instance().getTransportManager().setCurrentTransportMethod(new BTTransportMethod().getId());
		
	}

}
