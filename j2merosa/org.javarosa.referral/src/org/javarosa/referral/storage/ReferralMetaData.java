/**
 * 
 */
package org.javarosa.referral.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.referral.model.Referrals;

/**
 * @author Clayton Sims
 *
 */
public class ReferralMetaData extends MetaDataObject {

	/** The external name of the form these referrals are for **/
	String formName;
	
	public ReferralMetaData() {
	}
	
	/**
	 * @return the formName
	 */
	public String getFormName() {
		return formName;
	}

	/**
	 * @param formName the formName to set
	 */
	public void setFormName(String formName) {
		this.formName = formName;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
	 */
	public void setMetaDataParameters(Object originalObject) {
		Referrals ref = (Referrals)originalObject;
		this.formName = ref.getFormName();
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException
    {
    	super.readExternal(in, pf);
    	this.formName = in.readUTF();
    }
	
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
     */
	public void writeExternal(DataOutputStream out) throws IOException
    {
        super.writeExternal(out);
        out.writeUTF(this.formName);
    }

}
