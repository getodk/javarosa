package org.javarosa.properties;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.clforms.storage.Externalizable;
import org.javarosa.clforms.storage.IDRecordable;

public class Property implements Externalizable, IDRecordable{
    public String name;
    public String value;
    public int recordId;
    
    public void readExternal(DataInputStream in) throws IOException {
        byte[] inputarray = new byte[in.available()];
        in.readFully(inputarray);
        String fullString = new String(inputarray);
        name = fullString.substring(0, fullString.indexOf(","));
        value = fullString.substring(fullString.indexOf(",")+1,fullString.length());
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        out.writeChars(name + "," + value);
        out.close();

    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId; 
    }
}
