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

package org.javarosa.patient.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.patient.model.Patient;

public class PatientMetaDataObject extends MetaDataObject {

    private String patientName = ""; //the name of the FormData being referenced
	private int patientRecordId;
	private String patientId;

    
    public String toString(){
    	return new String (super.toString()+" name: "+this.patientName + " patientId: " + patientId);
    }
    
    /**
     * Creates an Empty meta data object
     */
    public PatientMetaDataObject()
    {
        
    }
    
    /**
     * Creates a meta data object for the patient data object given.
     * 
     * @param form The patient whose meta data this object will become
     */
    public PatientMetaDataObject(Patient patient)
    {
        this.patientName = patient.getName();
        this.patientRecordId = patient.getRecordId();
        this.patientId = patient.getPatientIdentifier();
        
    }
    
    /**
     * @param name Sets the name for the form this meta data object represents
     */
    public void setName(String name)
    {
        this.patientName = name;
    }
    
    /**
     * @return the name of the form represented by this meta data
     */
    public String getName()
    {
       return this.patientName; 
    }
    
    /**
     * @return the RMS Storage Id for the form this meta data represents
     */
    public int getPatientRecordId() {
    	return patientRecordId;
    }
    
    /** 
     * @return The Patient ID for the patient that this meta data represents
     */
    public String getPatientId() {
    	return patientId;
    }
    
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.DataInputStream)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
    	super.readExternal(in, pf);
    	
        this.patientRecordId = in.readInt();
        
        this.patientName = in.readUTF();
        this.patientId = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
    }

   
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
    	super.writeExternal(out);
    	
    	out.writeInt(this.getPatientRecordId());
    	
    	out.writeUTF(this.getName());
   
		ExtUtil.write(out, new ExtWrapNullable(this.patientId));
    }

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
     */
    public void setMetaDataParameters(Object object)
    {
        Patient patient = (Patient)object;
        this.setName(patient.getName());
    }
}
