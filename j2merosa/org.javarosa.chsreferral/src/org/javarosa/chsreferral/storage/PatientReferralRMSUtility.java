package org.javarosa.chsreferral.storage;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;

/**
 * 
 * The internal record store for patient referrals.
 * 
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
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
	
	/**
	 * Gets a patient referral based on the unique ID associated with it.
	 * 
	 * @param uid A String ID. Should be unique across the space of the Patient Records.
	 * @return A Patient Referral with the Unique ID provided.
	 * @throws DeserializationException If there is a problem with accessing the RMS
	 */
	public PatientReferral getRecordFromUid(String uid) throws DeserializationException {
		PatientReferral ref;
		IRecordStoreEnumeration en = this.enumerateRecords();
		while(en.hasNextElement()) {
			try {
				ref = this.retrieveFromRMS(en.nextRecordId());
				
				if(ref.getReferralId().equals(uid)) {
					return ref;
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new DeserializationException("Could not deserialize a referral from the RMS");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new DeserializationException("Could not deserialize a referral from the RMS");
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new DeserializationException("Could not deserialize a referral from the RMS");
			} catch (RecordStorageException e) {
				e.printStackTrace();
				throw new DeserializationException("Could not deserialize a referral from the RMS");
			}
			
		}
		
		return null;
	}
}
