package org.javarosa.properties;

import java.lang.Character;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.clforms.storage.Externalizable;
import org.javarosa.clforms.storage.IDRecordable;

/**
 * Property is an encapsulation of a record containing a property in the J2ME
 * RMS persistent storage system. It is responsible for serializing a name
 * value pair.
 * 
 * @author ctsims
 * @date Jan-20-2008
 * 
 */
public class Property implements Externalizable, IDRecordable{
    public String name;
    public String value;
    public int recordId;
   
    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.Externalizable#readExternal(DataInputStream)
     */
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

    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.Externalizable#writeExternal(DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException {
        String outputString = name + "," + value;
        for(int i = 0 ; i < outputString.length(); ++i) {
            out.writeByte((byte)outputString.charAt(i));
        }
        //out.writeChars();
        out.close();

    }
    
    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.IDRecordable#setRecordId(int)
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId; 
    }
}
