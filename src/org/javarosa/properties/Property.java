package org.javarosa.properties;

import java.lang.Character;

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
        String fullString = "";
        
        byte[] inputarray = new byte[in.available()];
        in.readFully(inputarray);
        
        for(int i = 0; i < inputarray.length ; i++ ) {
            char c = (char)inputarray[i];
            fullString = fullString + c;
        }
        //String fullString = new String(inputarray);
        name = fullString.substring(0, fullString.indexOf(","));
        value = fullString.substring(fullString.indexOf(",")+1,fullString.length());
        in.close();
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        String outputString = name + "," + value;
        for(int i = 0 ; i < outputString.length(); ++i) {
            out.writeByte((byte)outputString.charAt(i));
        }
        //out.writeChars();
        out.close();

    }
    
    public void setRecordId(int recordId) {
        this.recordId = recordId; 
    }
}
