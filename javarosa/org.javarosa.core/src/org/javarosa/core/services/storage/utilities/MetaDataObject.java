/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.services.storage.utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A MetaDataObject is a serialized meta data description of
 * an RMS record, that is used to provide identifying identification
 * for full records. 
 * 
 * @author Munier
 */
public abstract class MetaDataObject implements 
        MetaDataTyped, Externalizable, IDRecordable
{

    private int recordId = 0; //this objects ID in the RMS
    private int size = 0; //the size of the XForm object in the XForm RMS

    public MetaDataObject()
    {
    }
    
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataTyped#setSize(int)
     */
    public void setSize(int iSize)
    {
        this.size = iSize;
    }
    
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataTyped#getSize()
     */
    public int getSize()
    {
        return this.size;
    }
    
    //declare this here to work around Nokia JVM bug
    public abstract void setMetaDataParameters(Object originalObject);
    
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.IDRecordable#setRecordId(int)
     */
    public void setRecordId(int recordId)
    {
        this.recordId = recordId;
    }

    /**
     * Gets the Id of the current record
     * 
     * @return The Id of this record
     */
	public int getRecordId() {
		return recordId;
	}
	
	public String toString(){
		return new String ("ID: "+this.recordId+" size:"+this.size);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        this.recordId = in.readInt();
        this.size = in.readInt();
    }
	
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
     */
	public void writeExternal(DataOutputStream out) throws IOException
    {
        out.writeInt(this.recordId);
        out.writeInt(this.size);
    }
}