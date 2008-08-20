package org.javarosa.patientselect.search;

import java.io.*;
import java.util.*;
import org.javarosa.core.util.*;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.patientselect.object.ExternalizableObject;
import org.javarosa.patientselect.store.PatientStore;

public class BasicSearch implements ISearch, Externalizable {
	
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

	public void searchByBoth(String patientName, String patientCode) {
		
		if(patientName != null && patientCode != null){
			try {
				store.retrieveFromRMS(formId, externObject);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnavailableExternalizerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}

	public String searchByCode(String code) {
		
		if(code != null){
			try{
				store.retrieveDataIndex(code);
			}
			catch(Exception excep){
				excep.printStackTrace();
			}
		}
		return null;
	}

	public String searchByName(String patientName) {
		
		if(patientName != null){
			try {
				store.retrieveFromRMS(formId);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnavailableExternalizerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public String selectPatient(String patientId) {
		if(patientId != null){
			
		}
		return null;
	}
}