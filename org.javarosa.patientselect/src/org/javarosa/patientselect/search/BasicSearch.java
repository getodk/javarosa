package org.javarosa.patientselect.search;

import java.io.*;
import java.util.*;
import org.javarosa.core.util.*;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.patientselect.object.ExternalizableObject;
import org.javarosa.patientselect.store.PatientStore;

public class BasicSearch implements Externalizable {
	
	private int formId;
	Vector searchConditions;
	
	private PatientStore store = null;
	private ExternalizableObject externObject = null;
	
	public BasicSearch(){
		
		searchConditions = new Vector();	
		store = new PatientStore("PatientStore");
		externObject = new ExternalizableObject();
	}

	public BasicSearch(int formid, Vector searchCondition){
		
		this.formId = formid;
		this.searchConditions = searchCondition;
	}
	
	public int getFormId(){
		
		return this.formId;
	}
	
	public void setFormId(int formid){
		
		this.formId = formid;
	}
	
	public Vector getSearchProc(){
		
		Vector searchAlg = new Vector();
		
		Enumeration en = searchConditions.elements();
		while(en.hasMoreElements()) {
			
		}
		return searchAlg;
	}
	
	public void readExternal(DataInputStream in) throws IOException, InstantiationException, IllegalAccessException,UnavailableExternalizerException 
	{
		this.formId = in.readInt();
		searchConditions = ExternalizableHelper.readExternal(in, ExternalizableObject.class);
	}

	public void writeExternal(DataOutputStream out) throws IOException 
	{
		out.writeInt(this.formId);
		ExternalizableHelper.writeExternal(searchConditions, out );
	}

	public void searchByBoth(String patientName, String patientCode) throws IOException, IllegalAccessException, InstantiationException, UnavailableExternalizerException {
		if(patientName != null && patientCode != null){
			store.retrieveFromRMS(formId, externObject);
		}	
	}

	public String searchByCode(String code) throws IOException {
		if(code != null){
			store.retrieveByteDataFromRMS(formId);
		}
		return null;
	}

	public String searchByName(String patientName) throws IOException, IllegalAccessException, InstantiationException, UnavailableExternalizerException {
		if(patientName != null){
			store.retrieveFromRMS(formId);
		}
		return null;
	}

	public String selectPatient(String patientId) {
		if(patientId != null){
			
		}
		return null;
	}
}