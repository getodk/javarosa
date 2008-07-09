package org.javarosa.core.services.storage.utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
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

    public void setSize(int iSize)
    {
        this.size = iSize;
    }
    
    public int getSize()
    {
        return this.size;
    }
    
    //declare this here to work around Nokia JVM bug
    public abstract void setMetaDataParameters(Object originalObject);
    
    public void setRecordId(int recordId)
    {
        this.recordId = recordId;
    }

	public int getRecordId() {
		return recordId;
	}
	
	public String toString(){
		return new String ("ID: "+this.recordId+" size:"+this.size);
	}
	
    public void readExternal(DataInputStream in) throws IOException
    {
        this.recordId = in.readInt();
        this.size = in.readInt();
    }
	
	public void writeExternal(DataOutputStream out) throws IOException
    {
        out.writeInt(this.recordId);
        out.writeInt(this.size);
    }
}