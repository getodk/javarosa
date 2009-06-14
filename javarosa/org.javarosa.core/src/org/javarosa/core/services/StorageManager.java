/*
 * Copyright (C) 2009 JavaRosa
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

package org.javarosa.core.services;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.storage.IStorageProvider;
import org.javarosa.core.services.storage.RMSProvider;

/**
 * Manages StorageProviders for JavaRosa, which maintain persistent
 * data on a device.
 * 
 * Largely derived from Cell Life's RMSManager
 * 
 * @author Clayton Sims
 *
 */
public class StorageManager implements IService
{
    private Hashtable StorageRegistry = new Hashtable();
	private RMSProvider rmsProvider;
	
    /** Creates a new instance of StorageManager */
    public StorageManager()
    {
    	StorageRegistry = new Hashtable();
    }
    
    public String getName() {
    	return "Storage Manager";
    }
    
    /**
     * Registers a storage provider with this Manager. Access
     * to this provider in the future is granted from the provider's
     * registered name.
     * 
     * @param provider The storage provider to be registered
     */
    public void registerIStorageProvider(IStorageProvider provider)
    {
        if (this.StorageRegistry.containsKey(provider.getName()))
            return;
    
        this.StorageRegistry.put(provider.getName(), provider);            
    }
    
    /**
     * Retrieves a provider from the storage manager using its name
     * 
     * @param name The name of the provider to be retrieved
     * @return The StorageProvider, if it is currently registered
     * with this manager. null otherwise.
     */
    public IStorageProvider getProvider(String name)
    {
        if (this.StorageRegistry.containsKey(name))
            return (IStorageProvider)this.StorageRegistry.get(name);
        else
        {
            return null;
        }
    }
    
    
    /**
     * Gets the number of providers currently registered with this manager.
     * 
     * @return The number of providers currently registered
     */
    public int getNumberOfRegisteredProviders()
    {
        return this.StorageRegistry.size();
    }

    /**
     * Gets the names of all providers currently registered with this manager.
     * 
     * @return A vector containing strings that are the names of all providers
     * that are currently registered with this manager.
     */
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
     * RMS Storage is part of core, so it gets special treatment and can be
     * retrieved with a simple reference, rather than through the getProvider
     * interface.
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
