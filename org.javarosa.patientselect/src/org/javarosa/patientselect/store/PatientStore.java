package org.javarosa.patientselect.store;

import java.io.IOException;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patientselect.object.ExternalizableObject;

import de.enough.polish.util.ArrayList;

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
    
	private ArrayList patientList;
	int patIndex;
	
	public PatientStore(String name) {
		
		super(name, RMSUtility.RMS_TYPE_META_DATA);
		
		this.recordStoreName = name;
		
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
        
      //#if debug.output==verbose || debug.output==exception
        System.out.println("RMS SIZE (" + this.recordStoreName + ") : " + this.getNumberOfRecords());
        //#endif
        
        patientList = new ArrayList();
        
        patientList.add("Mark");
	}
	
	public void saveData(Object patData){
		
		try{
			if(patData != null){
				patientList.add(patData);
			}
		}
		catch(Exception excep){
			
			//#if debug.output==verbose || debug.output==exception
			excep.printStackTrace();
			//#endif
		}
		
	}
	
	public int retrieveDataIndex(Object searchData){
		
		patIndex = -1;
		
		try{
			if(searchData != null){
				
				if(patientList.contains(searchData)){
					patIndex =  patientList.indexOf(searchData);
					searchPatient(patIndex);
				}
			}
		}
		catch(Exception excep){
			//#if debug.output==verbose || debug.output==exception
			excep.printStackTrace();
			//#endif
		}
		return patIndex;
	}
	
	public String searchPatient(int searchIndex){
		
		patIndex = searchIndex;
		
		try{
			return patientList.get(patIndex).toString();
		}
		catch(Exception excep){
			
			//#if debug.output==verbose || debug.output==exception
			excep.printStackTrace();
			//#endif
			
			return null;
		}
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
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
		return formId;
	}
}
