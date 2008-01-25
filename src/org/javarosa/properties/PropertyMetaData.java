package org.javarosa.properties;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.clforms.storage.MetaDataObject;
import org.javarosa.clforms.storage.Model;

/**
 * PropertyMetaData is the Meta Data object used for Property records in the RMS
 * @author ctsims
 *
 */
public class PropertyMetaData extends MetaDataObject {
    private String name = "";
    
    public PropertyMetaData()
    {
    }
    
    /**
     * Creates a Property Meta Data object for a given property
     * @param property The property whose meta data will be captured
     */
    public PropertyMetaData(Property  property)
    {
        setMetaDataParameters(property);
    }
    
    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.MetaDataTyped#setMetaDataParemeters(Object)
     */
    public void setMetaDataParameters(Object originalObject) {
        Property property =(Property)originalObject;
        this.name = property.name;
    }
    
    /**
     * Gets the name of the property this object represents
     * @return The name of the property that this object is the meta data for
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of thie property that this object represents
     * @param name The name of the property that this object is the meta data for
     */
    public void setName(String name) {
        this.name = name;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.Externalizable#readExternal(DataInputStream)
     */
    public void readExternal(DataInputStream in) throws IOException
    {
        System.out.println("trying to readname Model");
        this.name = in.readUTF();
    }

    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.Externalizable#writeExternal(DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
        out.writeUTF(this.name);
    }
}
