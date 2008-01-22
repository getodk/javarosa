/*
 * MetaDataObject.java
 * 
 * Created on 2007/11/11, 12:20:26
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javarosa.clforms.storage;

/**
 *
 * @author Munier
 */
public interface MetaDataTyped 
{
   public abstract void setSize(int iSize);
   public abstract int getSize();
   public void setMetaDataParameters(Object originalObject);
}