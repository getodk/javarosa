package org.javarosa.referral.storage;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.core.model.storage.FormDataMetaData;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.UnavailableExternalizerException;
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
		md.setFormId(referrals.getFormId());
		super.writeToRMS(referrals, md);
	}
	
	public Referrals retrieveFromRMS(int formId) throws IOException, IllegalAccessException, InstantiationException, UnavailableExternalizerException {
		Referrals referrals = new Referrals();
		
		int refId = getReferralsId(formId);
		this.retrieveFromRMS(refId, referrals);
		
		return referrals;
	}
	
	public boolean containsFormReferrals(int formId) {
		if(getReferralsId(formId) == -1) {
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
	
	private int getReferralsId(int formId) {
		try {
			RecordEnumeration metaEnum = metaDataRMS.enumerateMetaData();
			while (metaEnum.hasNextElement()) {
				int i = metaEnum.nextRecordId();
				ReferralMetaData md = (ReferralMetaData)getMetaDataFromId(i);
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
