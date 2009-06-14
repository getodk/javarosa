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

package org.javarosa.core.services.storage;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.storage.utilities.IRecordStoreFactory;
import org.javarosa.core.services.storage.utilities.RMSUtility;


/**
 * RMSProvider allows for the registration of different utilities
 * which provide access to a device's RMS persistent storage.
 * 
 * @author Munier
 */
public class RMSProvider implements IStorageProvider
{
    private Hashtable RMSRegistry = new Hashtable();
    
    IRecordStoreFactory storeFactory;
    
    /** Creates a new instance of RMSProvider */
    public RMSProvider()
    {
        
    }
    
    /**
     * Registers an RMS utility with this provider.
     * 
     * @param utility The utility to be registered
     */
    public void registerRMSUtility(RMSUtility utility)
    {
    	registerRMSUtility(utility, utility.getName());
    }
    
    public void registerRMSUtility(RMSUtility utility, String name) {
        if (this.RMSRegistry.containsKey(name))
            return;
    
        this.RMSRegistry.put(name, utility);            
    	
    }
    
    public void unregisterRMSUtility(RMSUtility utility, String name) {
    	this.RMSRegistry.remove(name);
    }
    
    /**
     * 
     * @param name
     * @return
     * @throws NullPointerException
     */
    public RMSUtility getUtility(String name) throws NullPointerException
    {
        if (this.RMSRegistry.containsKey(name))
            return (RMSUtility)this.RMSRegistry.get(name);
        else
        {
        	return null;
        }
    }
    
    
    public int getNumberOfRegisteredUtilities()
    {
        return this.RMSRegistry.size();
    }

    
    public Vector getUtilityNames()
    {
       Vector utilityNames = new Vector();
       Enumeration en = this.RMSRegistry.elements();
       while (en.hasMoreElements())
       {
           RMSUtility currentUtility = (RMSUtility)en.nextElement();
           utilityNames.addElement(currentUtility.getName());
       }
       return utilityNames;
    }
    
    public String getName() {
    	return "RMS Provider";
    }
    
    /**
     * Sets the factory that can be used to produce new interfaces to
     * on-device record storage.
     * 
     * @param factory The factory that should be used. 
     */
    public void setRecordStoreFactory(IRecordStoreFactory factory) {
    	this.storeFactory = factory;
    }
    
    /**
     * @return The factory to be used to produce new interfaces to 
     * on device storage.
     */
    public IRecordStoreFactory getRecordStoreFactory() {
    	return this.storeFactory;
    }
}
