/*
 * Copyright (C) 2009 JavaRosa-Core Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core;

import java.util.Date;
import java.util.Hashtable;

import org.javarosa.core.api.IDaemon;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IIncidentLogger;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.IService;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.StorageManager;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.transport.storage.RmsStorage;
import org.javarosa.core.util.InactivityMonitor;
import org.javarosa.core.util.PrefixTree;
import org.javarosa.core.util.externalizable.CannotCreateObjectException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * JavaRosaServiceProvider is a singleton class that grants access to JavaRosa's
 * core services, Storage, Transport, and Property Management. New services can
 * also be registered with the Service Provider.
 * 
 * @author Brian DeRenzi
 * @author Clayton Sims
 *
 */
public class JavaRosaServiceProvider {
	protected static JavaRosaServiceProvider instance;
	
	private Hashtable daemons;

	private IDisplay display;
	
	private StorageManager storageManager;
    private ITransportManager transportManager;
    private PropertyManager propertyManager;
    
    private IIncidentLogger logger;
	
	Hashtable services;
	private PrefixTree prototypes;
	
	private InactivityMonitor imon;
	
	public JavaRosaServiceProvider() {
		services = new Hashtable();
		prototypes = new PrefixTree();
		daemons = new Hashtable();
	}
	
	public static JavaRosaServiceProvider instance() {
		if(instance == null) {
			instance = new JavaRosaServiceProvider();
		}
		return instance;
	}

	/**
	 * Initialize the platform.  Setup things like the RMS for the forms, the transport manager...
	 */
	public void initialize() {
		// For right now do nothing, to conserve memory we'll load Providers when they're asked for
	}

	/**
	 * Should be called by the midlet to set the display
	 * @param d - the j2me disply
	 */
	public void setDisplay(IDisplay d) {
		instance.display = d;
	}

	/**
	 * @return the display
	 */
	public IDisplay getDisplay() {
		return instance.display;
	}

	/**
	 * Display the view that is passed in.
	 * @param view
	 */
	public void showView(IView view) {
		instance.display.setView(view);
	}
	
	public StorageManager getStorageManager() {
			if(storageManager == null) {
				storageManager = new StorageManager();
				this.registerService(storageManager);
			}
			return storageManager;
	}
	
	public ITransportManager getTransportManager() {
		if(transportManager == null) {
			String[] classes = {
					"org.javarosa.core.services.transport.ByteArrayPayload",
					"org.javarosa.core.services.transport.MultiMessagePayload",
					"org.javarosa.core.services.transport.DataPointerPayload"
			};		
			registerPrototypes(classes);
			transportManager = new TransportManager(new RmsStorage());
			this.registerService(transportManager);
		}
		return transportManager;
	}
	
	public PropertyManager getPropertyManager() {
		if(propertyManager == null) {
			propertyManager = new PropertyManager();
			this.registerService(propertyManager);
		}
		return propertyManager;
	}
	

	public void registerDaemon(IDaemon daemon, String name) {
		daemons.put(name, daemon);
	}
	
	public IDaemon getDaemon(String name) {
		IDaemon daemon = (IDaemon)daemons.get(name);
		//Do we want to handle the null case with an exception, like with services?
		return daemon;
	}
	
	public void registerService(IService service) {
		services.put(service.getName(), service);
	}
	
	public IService getService(String serviceName) throws UnavailableServiceException {
		IService service = (IService)services.get(serviceName);
		if( service == null) {
			throw new UnavailableServiceException("The JavaRosaServiceProvider received a request for the service " + serviceName + ", which was not registered");
		} else {
			return service; 
		}
	}
	
	public void registerPrototype (String className) {
		prototypes.addString(className);
		
		try {
			PrototypeFactory.getInstance(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new CannotCreateObjectException(className + ": not found");
		}
	}
	
	public void registerPrototypes (String[] classNames) {
		for (int i = 0; i < classNames.length; i++)
			registerPrototype(classNames[i]);
	}
	
	public PrefixTree getPrototypes () {
		return prototypes;
	}
	
	public void registerIncidentLogger(IIncidentLogger logger) {
		this.logger= logger;
	}
	
	public IIncidentLogger getIncidentLogger() {
		return logger;
	}
	
	/**
	 * Posts the given data to an existing Incident Log, if one has
	 * been registered and if logging is enabled on the device. 
	 * 
	 * NOTE: This method makes a best faith attempt to log the given
	 * data, but will not produce any output if such attempts fail.
	 * 
	 * @param type The type of incident to be logged. 
	 * @param message A message describing the incident.
	 */
	public void logIncident(String type, String message) {
		if(JavaRosaPropertyRules.LOGS_ENABLED_YES.equals(this.getPropertyManager().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED))){
		if(logger != null) {
			logger.logIncident(type, message, new Date());
		} else {
			System.out.println(type + ": " + message);
		}
		}
	}
	
	
//	public void enableInactivityTimeout (int seconds) {
//		imon = new InactivityMonitor(this, seconds);
//	}
//	
//	public void activateInactivityTimeout () {
//		if (imon != null) {
//			imon.activate();
//		}
//	}
//
//	public void deactivateInactivityTimeout () {
//		if (imon != null) {
//			imon.deactivate();
//		}
//	}
//
//	public void notifyActivity () {
//		if (imon != null) {
//			imon.notifyActivity();
//		}
//	}
}
