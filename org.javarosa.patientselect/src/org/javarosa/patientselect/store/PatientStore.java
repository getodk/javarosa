package org.javarosa.patientselect.store;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.*;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patientselect.object.ExternalizableObject;

/**
* RMSUtility class for this project
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
    
	private Vector patientList;
	int patIndex;
	
	public PatientStore(String name) {
		
		super(name, RMSUtility.RMS_TYPE_META_DATA);
		
		this.recordStoreName = name;
		
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
        
        System.out.println("RMS SIZE (" + this.recordStoreName + ") : " + this.getNumberOfRecords());
        
        patientList = new Vector();
	}
	
	public void putData(String[] patData){
		try{
			if(patData != null){
				patientList.addElement(patData);
			}
		}
		catch(Exception excep){
			excep.printStackTrace();
		}
		
	}
	
	public int retrieveDataIndex(String data){
		
		patIndex = -1;
		
		try{
			if(data != null){
				if(patientList.contains(data)){
					patIndex =  patientList.indexOf(data);
					searchPatient(patIndex);
				}
			}
		}
		catch(Exception excep){
			excep.printStackTrace();
		}
		return patIndex;
	}
	
	public String searchPatient(int searchIndex){
		
		searchIndex = patIndex;
		try{
			return patientList.elementAt(searchIndex).toString();
		}
		catch(Exception excep){
			
			excep.printStackTrace();
			return null;
		}
	}
	
	/*
	**
	 * Writes the given form data to persistent storage
	 * @param form The form to be written
	 */
	public void writeToRMS(ExternalizableObject patientData) {
		PatientListMetaData md = new PatientListMetaData();
		
		md.setFormId(patientData.getFormId());
		super.writeToRMS(patientData, md);
	}
	
	public ExternalizableObject retrieveFromRMS(int formId) throws IOException, IllegalAccessException, InstantiationException, UnavailableExternalizerException {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
}
