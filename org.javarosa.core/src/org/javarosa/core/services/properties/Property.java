package org.javarosa.core.services.properties;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.IDRecordable;

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
    public Vector value;
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
        int nameindex = fullString.indexOf(","); 
        value = new Vector();
        if(nameindex == -1) {
    		//#if debug.output==verbose
            System.out.println("WARNING: Property in RMS with no value");
            //#endif
            name = fullString.substring(0, fullString.length());
        }
        else {
            name = fullString.substring(0, nameindex);
        // The format of the properties should be each one in a list, comma delimited
        String packedvalue = fullString.substring(fullString.indexOf(",")+1,fullString.length());
        while(packedvalue.length() != 0) {
            int index = packedvalue.indexOf(",");
            if(index == -1) {
                value.addElement(packedvalue);
                packedvalue = "";
            }
            else {
                value.addElement(packedvalue.substring(0,index));
                packedvalue = packedvalue.substring(index + 1, packedvalue.length());
            }
        }
        }
        in.close();
    }

    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.Externalizable#writeExternal(DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException {
        String outputString = name;
        // Note that this enumeration should contain at least one element, otherwise the
        // deserialization is invalid
        Enumeration en = value.elements();
        while(en.hasMoreElements()) {
            outputString += "," + (String)en.nextElement();
        }
        
        for(int i = 0 ; i < outputString.length(); ++i) {
            out.writeByte((byte)outputString.charAt(i));
        }
        out.close();

    }
    
    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.IDRecordable#setRecordId(int)
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId; 
    }
}
