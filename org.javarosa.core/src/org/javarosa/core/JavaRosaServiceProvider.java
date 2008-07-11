package org.javarosa.core;

import java.util.Hashtable;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.services.IService;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.StorageManager;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.transport.storage.RmsStorage;

/**
 * JavaRosaServiceProvider is a singleton class that grants access to JavaRosa's
 * core services, Storage, Transport, and Property Management. New services can
 * also be registered with the Service Provider.
 * 
 * @author Brian DeRenzi
 *
 */
public class JavaRosaServiceProvider {
	protected static JavaRosaServiceProvider instance;

	private Display display;
	
	private StorageManager storageManager;
    private TransportManager transportManager;
    private PropertyManager propertyManager;
	
	Hashtable services;
	
	public JavaRosaServiceProvider() {
		services = new Hashtable();
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
	public void setDisplay(Display d) {
		instance.display = d;
	}

	/**
	 * @return the display
	 */
	public Display getDisplay() {
		return instance.display;
	}

	/**
	 * Display the view that is passed in.
	 * @param view
	 */
	public void showView(Displayable view) {
		instance.display.setCurrent(view);
	}
	
	public StorageManager getStorageManager() {
			if(storageManager == null) {
				storageManager = new StorageManager();
				this.registerService(storageManager);
			}
			return storageManager;
	}
	
	public TransportManager getTransportManager() {
		if(transportManager == null) {
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
	
	public void registerService(IService service) {
		services.put(service, service.getName());
	}
	
	public IService getService(String serviceName) {
		return (IService)services.get(serviceName);
	}
}
