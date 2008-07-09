package org.javarosa.core.services;

/**
 * Largely derived from Cell Life's RMSManager
 * @author Clayton Sims
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.storage.IStorageProvider;
import org.javarosa.core.services.storage.RMSProvider;

public class StorageManager implements IService
{
    private Hashtable StorageRegistry = new Hashtable();
	private RMSProvider rmsProvider;
    /** Creates a new instance of StorageManager */
    public StorageManager()
    {
        
    }
    
    public String getName() {
    	return "Storage Manager";
    }
    
    public void registerIStorageProvider(IStorageProvider utility)
    {
        if (this.StorageRegistry.containsKey(utility.getName()))
            return;
    
        this.StorageRegistry.put(utility.getName(), utility);            
    }
    
    
    public IStorageProvider getProvider(String name) throws NullPointerException
    {
        if (this.StorageRegistry.containsKey(name))
            return (IStorageProvider)this.StorageRegistry.get(name);
        else
        {
            throw new NullPointerException();
        }
    }
    
    
    public int getNumberOfRegisteredUtilities()
    {
        return this.StorageRegistry.size();
    }

    
    public Vector getProviderNames()
    {
       Vector utilityNames = new Vector();
       Enumeration en = this.StorageRegistry.elements();
       while (en.hasMoreElements())
       {
           IStorageProvider currentProvider = (IStorageProvider)en.nextElement();
           utilityNames.addElement(currentProvider.getName());
       }
       return utilityNames;
    }
    
    /**
     * RMS Storage is part of core, so it gets special treatment.
     * 
     * @return An RMSStorageProvider instance
     */
    public RMSProvider getRMSStorageProvider() {
    	if(rmsProvider == null) {
    		rmsProvider = new RMSProvider();
    		registerIStorageProvider(rmsProvider);
    	}
    	return rmsProvider;
    }
}
