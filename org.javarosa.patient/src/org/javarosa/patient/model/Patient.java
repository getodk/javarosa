package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;
import org.javarosa.core.util.Map;

/**
 * The Patient data object contains information and records
 * for a single patient. These include static data types such as
 * a patient's id and name, and also historical data records for
 * patients, such as a list of weight measurements .
 * 
 * @author Clayton Sims
 *
 */
public class Patient implements Externalizable {

	/** RMS Record Id */
	private int recordId;
	
	/** Patient's given name */
	private String firstName;
	
	/** Patient's surname */
	private String surname;
	
	/** Patient's medical record ID */
	private String patientId;
	
	/* String->IPatientRecord */
	private Map records;
	
	//For now we're statically encoding record types.
	private NumericalRecord weightRecord = new NumericalRecord();
	
	private NumericalRecord heightRecord = new NumericalRecord();
	
	private NumericalRecord cd4CountRecord = new NumericalRecord();
	
	public Patient() { 
		records.put("weight", weightRecord);
		records.put("height", heightRecord);
		records.put("cd4count", cd4CountRecord);
	}
	
	/**
	 * @return a human readable version of the patient's full name
	 */
	public String getFullName() {
		return surname + ", " + firstName;
	}
	
	/**
	 * @return the recordId
	 */
	public int getRecordId() {
		return recordId;
	}

	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @return the patientId
	 */
	public String getPatientId() {
		return patientId;
	}

	/**
	 * @param patientId the patientId to set
	 */
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	
	public Object getRecord(String recordType) {
		if(recordType == "firstName") {
			return firstName;
		}
		//TODO: We need to figure out how to do these references better
		return null;
	}
	
	public Vector getRecordSet(String recordType, String selector) {
		IPatientRecord record = (IPatientRecord)records.get(recordType);
		if(record == null) {
			return new Vector();
		}
		return record.getHistoricalRecords(selector);
	}
	
	//public Vector getMeasurements(String type, String delimeted )

	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		recordId = in.readInt();
	
		firstName = ExternalizableHelper.readUTF(in);
		surname = ExternalizableHelper.readUTF(in);
		patientId = in.readUTF();
		
		weightRecord = new NumericalRecord();
		heightRecord = new NumericalRecord();
		cd4CountRecord = new NumericalRecord();
		
		weightRecord.readExternal(in);
		heightRecord.readExternal(in);
		cd4CountRecord.readExternal(in);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordId);
		
		ExternalizableHelper.writeUTF(out, firstName);
		ExternalizableHelper.writeUTF(out, surname);
		out.writeUTF(patientId);
		
		weightRecord.writeExternal(out);
		heightRecord.writeExternal(out);
		cd4CountRecord.writeExternal(out);
	}

}
