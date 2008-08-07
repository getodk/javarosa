package org.javarosa.referral.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

public class Referrals implements Externalizable {
	//The id of the form that these referrals are for
	private int formId;
	
	/** ReferralCondition */
	Vector referralConditions;
	
	public Referrals() {
		referralConditions = new Vector();
	}
	
	public Referrals(int formId , Vector referralConditions) {
		this.formId = formId;
		this.referralConditions = referralConditions;
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

	public Vector getPositiveReferrals(DataModelTree model, XFormAnswerDataSerializer serializer) {
		Vector referralStrings = new Vector();
		
		Enumeration en = referralConditions.elements();
		while(en.hasMoreElements()) {
			ReferralCondition condition = (ReferralCondition)en.nextElement();
			
			IAnswerData data = model.getDataValue(condition.getQuestionReference());
			
			// TODO: We should be parsing this better. Probably using the
			// XFormAnswerSerializer.
			Object serData = serializer.serializeAnswerData(data);
			if (serData != null) {
				if (serData instanceof String) {
					if (serData.equals(condition.getReferralValue())) {
						referralStrings.addElement(condition.getReferralText());
					}
				}
			}
		}

		return referralStrings;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		this.formId = in.readInt();
		referralConditions = ExternalizableHelper.readExternal(in, ReferralCondition.class);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(this.formId);
		ExternalizableHelper.writeExternal(referralConditions, out);	
	}
	
	
}
