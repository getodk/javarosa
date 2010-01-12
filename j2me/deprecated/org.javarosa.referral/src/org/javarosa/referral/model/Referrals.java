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

package org.javarosa.referral.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

public class Referrals implements Externalizable {
	public static final String STORAGE_KEY = "REFERRALS";
	
	//The id of the form that these referrals are for
	private String formName;
	
	/** ReferralCondition */
	Vector referralConditions;
	
	public Referrals() {
		referralConditions = new Vector();
	}
	
	public Referrals(String formName , Vector referralConditions) {
		if(formName == null) {
			throw new IllegalArgumentException("Cannot create a set of Referrals for a form with a null name");
		}
		this.formName = formName;
		this.referralConditions = referralConditions;
	}
	
	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public Vector getPositiveReferrals(DataModelTree model, XFormAnswerDataSerializer serializer) {
		Vector referralStrings = new Vector();
		
		Enumeration en = referralConditions.elements();
		while(en.hasMoreElements()) {
			ReferralCondition condition = (ReferralCondition)en.nextElement();
			
			IAnswerData data = model.getDataValue(condition.getQuestionReference());
			
			if (data instanceof SelectMultiData) {
				SelectMultiData mulData = (SelectMultiData) data;
				if (mulData != null && ((Vector) mulData.getValue()).size() > 0) {
					referralStrings.addElement(condition.getReferralText());
				}
				condition.getReferralText();
			} else {
				Object serData = serializer.serializeAnswerData(data);
				if (serData != null && data != null) {
					if (serData instanceof String) {
						if (serData.equals(condition.getReferralValue())) {
							referralStrings.addElement(condition
									.getReferralText());
						}
					}
				}
			}
		}

		return referralStrings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream
	 * )
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		this.formName = in.readUTF();
		referralConditions = (Vector)ExtUtil.read(in, new ExtWrapList(ReferralCondition.class));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(this.formName);
		ExtUtil.write(out, new ExtWrapList(referralConditions));
	}
	
	
}
