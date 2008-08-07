package org.javarosa.referral.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.model.xform.XPathReference;

public class ReferralCondition implements Externalizable {
	String referralValue;
	XPathReference questionReference;
	
	String referralText;
	
	public ReferralCondition() {
		
	}
	
	public ReferralCondition(String referralValue, String referralText, XPathReference questionReference) {
		this.referralValue = referralValue;
		this.referralText = referralText;
		this.questionReference = questionReference;
	}

	/**
	 * @return the referralValue
	 */
	public String getReferralValue() {
		return referralValue;
	}

	/**
	 * @param referralValue the referralValue to set
	 */
	public void setReferralValue(String referralValue) {
		this.referralValue = referralValue;
	}
	
	/**
	 * @return the referralText
	 */
	public String getReferralText() {
		return referralText;
	}

	/**
	 * @param referralText the referralText to set
	 */
	public void setReferralText(String referralText) {
		this.referralText = referralText;
	}	

	/**
	 * @return the questionReference
	 */
	public XPathReference getQuestionReference() {
		return questionReference;
	}

	/**
	 * @param questionReference the questionReference to set
	 */
	public void setQuestionReference(XPathReference questionReference) {
		this.questionReference = questionReference;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		referralValue = ExternalizableHelper.readUTF(in);
		referralText = ExternalizableHelper.readUTF(in);
		
		questionReference = new XPathReference();
		questionReference.readExternal(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out,referralValue);
		ExternalizableHelper.writeUTF(out,referralText);
		
		questionReference.writeExternal(out);
	}
}
