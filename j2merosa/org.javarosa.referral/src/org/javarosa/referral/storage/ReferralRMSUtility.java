package org.javarosa.referral.storage;

import java.io.IOException;

import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.referral.model.Referrals;

public class ReferralRMSUtility extends RMSUtility {

	public ReferralRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}
	
	public static String getUtilityName() {
		return "REFERRAL_RMS_UTILITY";
	}
	
	/**
	 * Writes the given form data to persistent storage
	 * @param form The form to be written
	 */
	public void writeToRMS(Referrals referrals) {
		ReferralMetaData md = new ReferralMetaData();
		md.setFormName(referrals.getFormName());
		super.writeToRMS(referrals, md);
	}
	
	public Referrals retrieveFromRMS(String formName) throws IOException, IllegalAccessException, InstantiationException, DeserializationException {
		Referrals referrals = new Referrals();
		
		int refId = getReferralsId(formName);
		this.retrieveFromRMS(refId, referrals);
		
		return referrals;
	}
	
	public boolean containsFormReferrals(String formName) {
		if(getReferralsId(formName) == -1) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the meta data object for the record with the given Id
	 * 
	 * @param recordId The id of the record whose meta data is to be returned
	 * @return The meta data of the record with the given Id
	 */
	private ReferralMetaData getMetaDataFromId(int recordId) {
		ReferralMetaData referralMetaData = new ReferralMetaData();
		this.retrieveMetaDataFromRMS(recordId, referralMetaData);
		return referralMetaData;
	}
	
	private int getReferralsId(String formName) {
		try {
			IRecordStoreEnumeration metaEnum = metaDataRMS.enumerateMetaData();
			while (metaEnum.hasNextElement()) {
				int i = metaEnum.nextRecordId();
				ReferralMetaData md = (ReferralMetaData)getMetaDataFromId(i);
				if(md.getFormName().equals(formName)) {
					return md.getRecordId();
				}
			}
		} catch (RecordStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
}
