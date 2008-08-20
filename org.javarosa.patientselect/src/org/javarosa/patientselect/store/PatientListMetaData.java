package org.javarosa.patientselect.store;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patientselect.object.ExternalizableObject;
import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
* This encapsulates the data to be passed with the patient Data
* 
* 
* @author Mark Gerard
*
*/
public class PatientListMetaData extends MetaDataObject {
	
	private int formId;
	private int patientId;
	private String patientName;
	
	public PatientListMetaData(){
		
	}
	
	public int getFormId(){
		
		return formId;
	}
	
	public int getPatientId(){
		
		return patientId;
		
	}
	
	private String getPatientName(){
		
		return patientName;
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
