	
package org.javarosa.core.services.storage;

/*
 * RMSProvider.java
 *
 * Created on September 13, 2007, 8:59 PM
 *
 * To change this template, choose Tools | Template Provider
 * and open the template in the editor.
 */

/**
 *
 * @author Munier
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.storage.utilities.RMSUtility;

public class RMSProvider implements IStorageProvider
{
    private Hashtable RMSRegistry = new Hashtable();
    /** Creates a new instance of RMSProvider */
    public RMSProvider()
    {
        
    }
    
    public void registerRMSUtility(RMSUtility utility)
    {
        if (this.RMSRegistry.containsKey(utility.getName()))
            return;
    
        this.RMSRegistry.put(utility.getName(), utility);            
    }
    
    
    public RMSUtility getUtility(String name) throws NullPointerException
    {
        if (this.RMSRegistry.containsKey(name))
            return (RMSUtility)this.RMSRegistry.get(name);
        else
        {
            throw new NullPointerException();
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
