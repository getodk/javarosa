/**
 * 
 */
package org.javarosa.referral.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
    public void readExternal(DataInputStream in) throws IOException
    {
    	super.readExternal(in);
    	this.formId = in.readInt();
    }
	
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
     */
	public void writeExternal(DataOutputStream out) throws IOException
    {
        super.writeExternal(out);
        out.writeInt(this.formId);
    }

}
