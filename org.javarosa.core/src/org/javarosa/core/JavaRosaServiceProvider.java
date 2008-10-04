package org.javarosa.core;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.services.IService;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.StorageManager;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.core.services.transport.storage.RmsStorage;
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

	private Display display;
	
	private StorageManager storageManager;
    private ITransportManager transportManager;
    private PropertyManager propertyManager;
	
	Hashtable services;
	private PrefixTree prototypes;
	
	public JavaRosaServiceProvider() {
		services = new Hashtable();
		prototypes = new PrefixTree();
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
	
	public ITransportManager getTransportManager() {
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
	
	public PrefixTree getPrototypes () {
		return prototypes;
	}
}
