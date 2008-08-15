package org.javarosa.patientselect.store;

import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import org.javarosa.core.services.storage.utilities.*;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patientselect.activity.PatientListActivity;
import org.javarosa.patientselect.object.ExternalizableObject;
import org.javarosa.referral.model.Referrals;
import org.javarosa.referral.storage.ReferralMetaData;

public class PatientStore extends RMSUtility  implements RecordListener {

	private String recordStoreName = "";
	private int iType = RMSUtility.RMS_TYPE_STANDARD;
	
	protected RMSUtility metaDataRMS;
    protected RecordStore recordStore = null;
	
	public PatientStore(String name) {
		
		super(name, RMSUtility.RMS_TYPE_META_DATA);
		
		this.recordStoreName = name;
		
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
        
        System.out.println("RMS SIZE (" + this.recordStoreName + ") : " + this.getNumberOfRecords());
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
