package org.javarosa.core.services.storage;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
        if (this.RMSRegistry.containsKey(utility.getName()))
            return;
    
        this.RMSRegistry.put(utility.getName(), utility);            
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
    
}
