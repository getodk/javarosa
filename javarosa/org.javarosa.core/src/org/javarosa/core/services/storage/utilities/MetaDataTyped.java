/*
 * MetaDataObject.java
 * 
 * Created on 2007/11/11, 12:20:26
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javarosa.core.services.storage.utilities;

/**
 * MetaDataTyped objects represent meta data for some object
 * @author Munier
 */
public interface MetaDataTyped 
{
	/**
	 * Sets the size of the meta data
	 * @param iSize Meta data size
	 */
   public abstract void setSize(int iSize);
   
   /**
    * Gets the size of the meta data
    * @return The size of the meta data
    */
   public abstract int getSize();
   
    /* Workaround for Nokia JVM bug */
   public void setMetaDataParameters(Object originalObject);
}