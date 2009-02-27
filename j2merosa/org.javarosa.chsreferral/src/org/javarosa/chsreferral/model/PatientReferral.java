package org.javarosa.chsreferral.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A patient referral is a persistent record associated with a patient.
 * A referral represents that a patient was referred somewhere, and will
 * be followed up with to resolve the issue that the referral was issued
 * for. 
 * 
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
public class PatientReferral implements Externalizable, IDRecordable {
	private String type;
	private Date dateReferred;
	private String referralId;
	private int patientId;
	
	private int recordId;
	
	private boolean pending = true;
	
	/**
	 * NOTE: Should only be used for deserialization.
	 */
	public PatientReferral() {
		
	}
	
	public PatientReferral(String type, Date dateReferred, String referralId, int patientId) {
		this.type = type;
		this.dateReferred = dateReferred;
		this.referralId = referralId;
		this.patientId = patientId;
	}

	/**
	 * @return the internal record ID of the patient that this referral is associated with
	 */
	public int getPatientId() {
		return patientId;
	}

	/**
	 * @return The Type of referral issued to the patient.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the dateReferred
	 */
	public Date getDateReferred() {
		return dateReferred;
	}

	/**
	 * @return the referralId
	 */
	public String getReferralId() {
		return referralId;
	}
	
	public boolean isPending() {
		return this.pending;
	}
	
	public void close() {
		this.pending = false;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		this.type  = ExtUtil.readString(in);
		this.dateReferred = ExtUtil.readDate(in);
		this.referralId = ExtUtil.readString(in);
		this.patientId = ExtUtil.readInt(in);
		this.recordId = ExtUtil.readInt(in);
		this.pending = ExtUtil.readBool(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out,type);
		ExtUtil.writeDate(out, dateReferred);
		ExtUtil.writeString(out, referralId);
		ExtUtil.writeNumeric(out, patientId);
		ExtUtil.writeNumeric(out, recordId);
		ExtUtil.writeBool(out, pending);
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
	
	public int getRecordId() {
		return recordId;
	}
	
	

}
