package org.javarosa.chsreferral.storage;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;

public class PatientReferralRMSUtility extends RMSUtility {

	public PatientReferralRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_STANDARD);
	}
	
	public static String getUtilityName() {
		return "PATIENT_REFERRAL_RMS_UTILITY";
	}
	
	/**
	 * Writes the given form data to persistent storage
	 * @param form The form to be written
	 */
	public void writeToRMS(PatientReferral referral) {
		super.writeToRMS(referral, null);
	}
	
	public PatientReferral retrieveFromRMS(int id) throws IOException, IllegalAccessException, InstantiationException, DeserializationException {
		PatientReferral referral = new PatientReferral();
		
		this.retrieveFromRMS(id, referral);
		
		return referral;
	}
	
	public Vector getPendingReferrals() throws DeserializationException {
		Vector pending = new Vector();
		IRecordStoreEnumeration en = enumerateRecords();
		while(en.hasNextElement()) {
			byte[] next;
			try {
				next = en.nextRecord();
				PatientReferral ref = new PatientReferral();
			
				ExtUtil.deserialize(next, ref);
				
				if(ref.isPending()) {
					pending.addElement(ref);
				}
			} catch (RecordStorageException e) {
				e.printStackTrace();
				throw new DeserializationException("Could not deserialize a referral from the RMS");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DeserializationException("Could not deserialize a referral from the RMS");
			}
		}
		
		return pending;
	}
}
