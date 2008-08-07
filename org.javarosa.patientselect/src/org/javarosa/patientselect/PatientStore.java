package org.javarosa.patientselect;

import org.javarosa.core.services.storage.utilities.*;

public class PatientStore  extends RMSUtility {

	private String recordStoreName = "";
	private int iType = RMSUtility.RMS_TYPE_STANDARD;
	
	public PatientStore(String name, int type) {
		
		super(name, type);
		
		this.recordStoreName = name;
		this.iType = type;
		
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
        
        System.out.println("RMS SIZE (" + this.recordStoreName + ") : " + this.getNumberOfRecords());
	}

}
