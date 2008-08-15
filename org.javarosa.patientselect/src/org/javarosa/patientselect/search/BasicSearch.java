package org.javarosa.patientselect.search;

import java.io.*;
import java.util.*;
import org.javarosa.core.util.*;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.patientselect.object.ExternalizableObject;

public class BasicSearch implements Externalizable {
	
	private int formId;
	Vector searchConditions;
	
	public BasicSearch(){
		
		searchConditions = new Vector();	
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

	public void searchByBoth(String patientName, String patientCode) {
		if(patientName != null && patientCode != null){
			
		}
		
	}

	public String searchByCode(String code) {
		if(code != null){
			
		}
		return null;
	}

	public String searchByName(String patientName) {
		if(patientName != null){
			
		}
		return null;
	}

	public String selectPatient(String patientId) {
		if(patientId != null){
			
		}
		return null;
	}


}