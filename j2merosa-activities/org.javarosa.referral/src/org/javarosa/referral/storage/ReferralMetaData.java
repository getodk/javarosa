/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
