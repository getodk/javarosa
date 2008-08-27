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
	
	/*
	 * Utility constructor to initialize objects
	 */
	public BasicSearch(){
		
		searchConditions = new Vector();	
		store = new PatientStore("PatientStore");
		externObject = new ExternalizableObject();
	}

	/*
	 * Utility Constructor to set formId
	 */
	public BasicSearch(int formid, Vector searchCondition){
		
		this.formId = formid;
		this.searchConditions = searchCondition;
	}
	
	/*
	 * Property to get the formId
	 */
	public int getFormId(){
		
		return this.formId;
	}
	
	/*
	 * Property to return the formId
	 */
	public void setFormId(int formid){
		
		this.formId = formid;
	}
	
	/*
	 * Method to set the search Procedure algorithm
	 */
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
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			} catch (IllegalAccessException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			} catch (InstantiationException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			} catch (UnavailableExternalizerException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
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
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			} catch (IllegalAccessException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			} catch (InstantiationException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			} catch (UnavailableExternalizerException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
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