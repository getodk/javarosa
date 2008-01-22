package org.javarosa.properties;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.clforms.storage.MetaDataObject;
import org.javarosa.clforms.storage.Model;

public class PropertyMetaData extends MetaDataObject {
    private String name = "";
    
    public PropertyMetaData()
    {
    }
    
    public PropertyMetaData(Property  property)
    {
        setMetaDataParameters(property);
    }
    
    public void setMetaDataParameters(Object originalObject) {
        Property property =(Property)originalObject;
        this.name = property.name;
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void readExternal(DataInputStream in) throws IOException
    {
        System.out.println("trying to readname Model");
        this.name = in.readUTF();
    }

    public void writeExternal(DataOutputStream out) throws IOException
    {
        out.writeUTF(this.name);
    }
}
