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

package org.javarosa.core.services.properties;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javarosa.core.services.storage.IMetaData;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Property is an encapsulation of a record containing a property in the J2ME
 * RMS persistent storage system. It is responsible for serializing a name
 * value pair.
 *
 * @author ctsims
 * @date Jan-20-2008
 *
 */
public class Property implements Persistable, IMetaData {
    public String name;
    public List<String> value;
    public int recordId = -1;

    /** (non-Javadoc)
     *  @see org.javarosa.core.util.externalizable.singlequestionscreen.storage.Externalizable#readExternal(DataInputStream)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
        StringBuilder b = new StringBuilder();

        byte[] inputarray = new byte[in.available()];
        in.readFully(inputarray);

        for(int i = 0; i < inputarray.length ; i++ ) {
            char c = (char)inputarray[i];
            b.append(c);
        }
        String fullString = b.toString();
        int nameindex = fullString.indexOf(",");
        value = new ArrayList<String>();
        if(nameindex == -1) {
    		//#if debug.output==verbose
            System.out.println("WARNING: Property in RMS with no value:"+fullString);
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
                value.add(packedvalue);
                packedvalue = "";
            }
            else {
                value.add(packedvalue.substring(0,index));
                packedvalue = packedvalue.substring(index + 1, packedvalue.length());
            }
        }
        }
        in.close();
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.util.externalizable.singlequestionscreen.storage.Externalizable#writeExternal(DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException {
        String outputString = name;
        // Note that this enumeration should contain at least one element, otherwise the
        // deserialization is invalid
        for ( String v : value ) {
           outputString += "," + v;
        }

        for(int i = 0 ; i < outputString.length(); ++i) {
            out.writeByte((byte)outputString.charAt(i));
        }
        out.close();

    }

    /** (non-Javadoc)
     *  @see org.javarosa.singlequestionscreen.storage.IDRecordable#setRecordId(int)
     */
    public void setID(int recordId) {
        this.recordId = recordId;
    }

    public int getID () {
    	return recordId;
    }

	public HashMap<String,Object> getMetaData() {
		HashMap<String,Object> metadata = new HashMap<String,Object>();
		String[] fields = getMetaDataFields();
		for (int i = 0; i < fields.length; i++) {
			metadata.put(fields[i], getMetaData(fields[i]));
		}
		return metadata;
	}

	public Object getMetaData(String fieldName) {
		if (fieldName.equals("NAME")) {
			return name;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String[] getMetaDataFields() {
		return new String[] {"NAME"};
	}
}
