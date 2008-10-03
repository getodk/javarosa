package org.javarosa.patientselect.store;

import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;

import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.patientselect.object.ExternalizableObject;

import de.enough.polish.util.ArrayList;

/**
* Temporary patient store to simulate the storing of patients
* 
* 
* @author Mark Gerard
*
*/
public class PatientStore extends RMSUtility  implements RecordListener {

	private String recordStoreName = "";
	private int iType = RMSUtility.RMS_TYPE_STANDARD;
	
	protected RMSUtility metaDataRMS;
    protected RecordStore recordStore = null;
    
	private ArrayList patientList;
	
	
	public PatientStore(String name) {
		
		super(name, RMSUtility.RMS_TYPE_META_DATA);
		
		this.recordStoreName = name;
		
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();

        //Simulated Patient Data
        
        String pat0 = "Gerard Mark, M/23, Genius, Reading, Elgon, 23/08/2008, 05/09/2008";
        String pat1 = "Clayton Sims, C/20, Code, Programming, MIT, 25/08/2008, 10/09/2008";
        String pat2 = "Jonathan Jackson, J/40, Manager, Company, Massechussets, 02/06/2008, 04/07/2008";
        
        //Simulated Patient Store
        
        patientList = new ArrayList();
        patientList.add(pat0);
        patientList.add(pat1);
        patientList.add(pat2);
        
        //#if debug.output==verbose || debug.output==exception
        
        	System.out.println("RMS SIZE (" + this.recordStoreName + ") : " + this.getNumberOfRecords());
        	System.out.println("ARRAY LIST SIZE ("+ this.patientList + "): " + this.patientList.size());
        	
        //#endif
        
	}
	
	public void saveData(Object patData){
		
		try{
			if(patData != null){
				
				patientList.add(patData);
			}
			
			//#if debug.output==verbose || debug.output==exception
				System.out.println("ARRAY LIST SIZE ("+ this.patientList + "): " + this.patientList.size());
			//#endif
		}
		catch(Exception excep){
			
			//#if debug.output==verbose || debug.output==exception
			excep.printStackTrace();
			//#endif
		}
		
	}
	
	public Object retrieveDataIndex(Object searchData){
		
		Object returnedData = null;
		
		try{
			
			if(searchData != null){
				
				System.out.println("Entering search mode in [store]");
				
				if(patientList.contains(searchData)){
					
					int patientIndex = patientList.indexOf(searchData);
					
					System.out.println("The index of the Patient Is:" + patientIndex);
					
					returnedData = patientList.get(patientIndex);
				}
			}
		}
		catch(Exception excep){
			//#if debug.output==verbose || debug.output==exception
			excep.printStackTrace();
			//#endif
		}
		
		System.out.println("Exiting search mode in [store]");
		
		return returnedData;
	}

	private ArrayList searchPatient(String searchData) {
		
		ArrayList matchingPatients = new ArrayList();
		for(int i = this.patientList.size();--i >= 0;){
			String result = (String) this.patientList.get(i);
			
			if(result.indexOf(searchData)!= -1){
				matchingPatients.add(result);
			}
		}
		return matchingPatients;
	}
	
	/*
	**
	 * Writes the given form data to persistent storage
	 * @param form The form to be written
	 */
	public void writeToRMS(ExternalizableObject patientData) {
		
		PatientListMetaData patMetaDataObject = new PatientListMetaData();
		
		patMetaDataObject.setFormId(patientData.getFormId());
		super.writeToRMS(patientData, patMetaDataObject);
	}
	
	public ExternalizableObject retrieveFromRMS(int formId) throws IOException, IllegalAccessException, InstantiationException, DeserializationException {
		
		ExternalizableObject patData = new ExternalizableObject();
		
		int patId = getPatientDataId(formId);
		this.retrieveFromRMS(patId, patData);
		
		return patData;
	}
	
	private PatientListMetaData getMetaDataFromId(int recordId) {
		
		PatientListMetaData patientMetaData = new PatientListMetaData();
		this.retrieveMetaDataFromRMS(recordId, patientMetaData);
		
		return patientMetaData;
	}
	
	private int getPatientDataId(int formId) {
		
		try {
			
			RecordEnumeration metaEnum = metaDataRMS.enumerateMetaData();
			
			while (metaEnum.hasNextElement()) {
				
				int i = metaEnum.nextRecordId();
				PatientListMetaData md = (PatientListMetaData)getMetaDataFromId(i);
				
				if(md.getFormId() == formId) {
					return md.getRecordId();
				}
			}
			
		} catch (InvalidRecordIDException e) {
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
		return formId;
	}
}
