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

/*
 * MetaDataObject.java
 * 
 * Created on 2007/11/11, 12:20:26
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javarosa.core.services.storage.utilities;

/* DEPRECATED */


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