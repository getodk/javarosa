package org.javarosa.patientselect.store;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.javarosa.patientselect.*;
import org.javarosa.patientselect.object.ExternalizableObject;

import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.UnavailableExternalizerException;

public class PatientListMetaData extends MetaDataObject {
	
	private int formId;
	
	public PatientListMetaData(){
		
	}
	
	public int getFormId(){
		
		return formId;
	}
	
	public void setFormId(int formid)
	{
		this.formId = formid;
	}
	
	public void setMetaDataParameters(Object originalObject) 
	{
		
		ExternalizableObject ref = (ExternalizableObject)originalObject;
		this.formId = ref.getFormId();
		
	}
	
    public void readExternal(DataInputStream in) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException
    {
    	super.readExternal(in);
    	this.formId = in.readInt();
    }
    
    public void writeExternal(DataOutputStream out) throws IOException
    {
        super.writeExternal(out);
        out.writeInt(this.formId);
    }


}
