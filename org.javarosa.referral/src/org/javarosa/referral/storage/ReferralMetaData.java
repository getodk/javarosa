/**
 * 
 */
package org.javarosa.referral.storage;

import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.referral.model.Referrals;

/**
 * @author Clayton Sims
 *
 */
public class ReferralMetaData extends MetaDataObject {

	int formId;
	
	public ReferralMetaData() {
	}
	
	/**
	 * @return the formId
	 */
	public int getFormId() {
		return formId;
	}

	/**
	 * @param formId the formId to set
	 */
	public void setFormId(int formId) {
		this.formId = formId;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
	 */
	public void setMetaDataParameters(Object originalObject) {
		Referrals ref = (Referrals)originalObject;
		this.formId = ref.getFormId();
	}

}
