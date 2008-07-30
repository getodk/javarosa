package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Map;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.entitymgr.model.EntityFieldList;
import org.javarosa.entitymgr.model.EntityFieldValueList;
import org.javarosa.entitymgr.model.IEntity;
import org.javarosa.util.Utilities;

/**
 * Encapsulates demographic data about a patient and contains 
 * information and records for a single patient. These include 
 * static data types such as a patient's id and name, and also 
 * historical data records for patients, such as a list of weight
 * measurements . 
 * 
 * @author daniel
 *
 */
public class Patient implements IEntity {
	

	private static final int INVALID_RECORD_ID = -1;
	private static final String NULL_DISPLAY_VALUE = "";

	/** RMS Record Id */
	private int recordId = INVALID_RECORD_ID;
	
	/** Patient's medical record ID. Is made Integer instead of int because new patients dont have it. */
	Integer patientId; //patientid 
	String prefix;
	
	/** Patient's Family name */
	String familyName;
	/** Optional Middle Name */
	String middleName;
	
	/** Patient's surname */
	String givenName;
	String gender;
	Date birthDate;
	String patientIdentifier;
	Vector attributes; //Not serialized since we can generate it on the fly.
	boolean isNewPatient;
	

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
	 * Gets the patient whole name which is a concatenation of the
	 * given, middle and family names.
	 * 
	 * @return
	 */
	public String getName(){
		String s="";

		if(getGivenName() != null && getGivenName().length() != 0)
			s += " " + getGivenName();
		
		if(getMiddleName() != null && getMiddleName().length() != 0)
			s += " " + getMiddleName();

		if(getFamilyName() != null && getFamilyName().length() != 0)
			s += " " + getFamilyName();

		return s;
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
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}
	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the birthDate
	 */
	public Date getBirthDate() {
		return birthDate;
	}

	/**
	 * @param birthDate the birthDate to set
	 */
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	/**
	 * Determines if this is a new record.
	 * @return true if the record is new.
	 */
	public boolean isNew(){
		return getRecordId() == INVALID_RECORD_ID;
	}
	

	public boolean isNewPatient() {
		return isNewPatient;
	}

	public void setNewPatient(boolean isNewPatient) {
		this.isNewPatient = isNewPatient;
	}

	/**
	 * Gets the current patient's Id
	 * @return An Id number representing this patient. Negative ID's are 
	 * assigned to new patients that have not been synchronized with a server.
	 */
	public Integer getPatientId() {
		//For now, new patients have negative ids. assuming server does not assign negatives
		//We use recordid value because is takes care of generating new ids for us.
		if(isNewPatient())
			return new Integer(-getRecordId());
		
		return patientId;
	}

	/**
	 * @param patientId the patientId to set
	 */
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	

	public Object getRecord(String recordType) {
		if(recordType == "givenName") {
			return givenName;
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

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return the patientIdentifier
	 */
	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	/**
	 * @param patientIdentifier the patientIdentifier to set
	 */
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public String toString() {
		String s;

		if(getPrefix() != null && getPrefix().length() != 0)
			s = getPatientIdentifier() + " " + getPrefix();
		else
			s = getPatientIdentifier();

		if(getGivenName() != null && getGivenName().length() != 0)
			s += " " + getGivenName();
		
		if(getMiddleName() != null && getMiddleName().length() != 0)
			s += " " + getMiddleName();

		if(getFamilyName() != null && getFamilyName().length() != 0)
			s += " " + getFamilyName();

		if(isNewPatient())
			s += " (NEW)";

		if(s == null)
			s = "NAMELESS PatientId="+getPatientId();
		return s;
	}

	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		recordId = in.readInt();
	
		setPatientId(ExternalizableHelper.readInteger(in));
		setPrefix(ExternalizableHelper.readUTF(in));
		setFamilyName(ExternalizableHelper.readUTF(in));
		setMiddleName(ExternalizableHelper.readUTF(in));
		setGivenName(ExternalizableHelper.readUTF(in));
		setGender(ExternalizableHelper.readUTF(in));
		setBirthDate(ExternalizableHelper.readDate(in));
		setPatientIdentifier(ExternalizableHelper.readUTF(in));
		setNewPatient(in.readBoolean());

		
		weightRecord = new NumericalRecord();
		heightRecord = new NumericalRecord();
		cd4CountRecord = new NumericalRecord();
		
		weightRecord.readExternal(in);
		heightRecord.readExternal(in);
		cd4CountRecord.readExternal(in);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordId);
			
		ExternalizableHelper.writeInteger(out,getPatientId());
		ExternalizableHelper.writeUTF(out, getPrefix());
		ExternalizableHelper.writeUTF(out, getFamilyName());
		ExternalizableHelper.writeUTF(out, getMiddleName());
		ExternalizableHelper.writeUTF(out, getGivenName());
		ExternalizableHelper.writeUTF(out, getGender());
		ExternalizableHelper.writeDate(out, getBirthDate());
		ExternalizableHelper.writeUTF(out, getPatientIdentifier());
		out.writeBoolean(isNewPatient());
		
		weightRecord.writeExternal(out);
		heightRecord.writeExternal(out);
		cd4CountRecord.writeExternal(out);
	}
	/** 
	 * Gets a list of patient attributes.
	 */
	public Vector getAttributes(){
		if(attributes == null)
			setDefaultAttributes();
		return attributes;
	}
	
	public void setIdentifier(String identifier){
		setPatientIdentifier(identifier);
	}
	
	public String getIdentifier(){
		return getPatientIdentifier();
	}
	
	public Integer getId(){
		return getPatientId();
	}
	
	public void setId(Integer id){
		setPatientId(id);
	}
	
	/**
	 * Adds a patient attribute. This is the value appended to the atttribute name.
	 * eg. "WHO Stage: II", "Last Visit Date: 01/01/2009", etc.
	 * 
	 * @param attribute
	 */
	public void addAttribute(String attribute){
		if(attributes == null)
			setDefaultAttributes();
		attributes.addElement(attribute);
	}
	
	public void addAttributes(EntityFieldList fields, EntityFieldValueList fieldVals){
		if(attributes == null)
			setDefaultAttributes();
		
		/*for(int i=0; i<fields.size(); i++){
			EntityField field = fields.getField(i);
			if(data.containsQuestion(field.getName()))
				data.setValue(field.getName(), fieldVals.getPatintFiledValue(field.getId(), getPatientId()));
		}
		
		attributes.addElement(attribute);*/
	}
	
	private void setDefaultAttributes(){
		attributes = new Vector();
		attributes.addElement("Identifier: " + (getPatientIdentifier() != null ? getPatientIdentifier() : NULL_DISPLAY_VALUE));
		attributes.addElement("Prefix: " + (getPrefix() != null ? getPrefix() : NULL_DISPLAY_VALUE));
		attributes.addElement("FamilyName: " + (getFamilyName() != null ? getFamilyName() : NULL_DISPLAY_VALUE));
		attributes.addElement("MiddleName: " + (getMiddleName() != null ? getMiddleName() : NULL_DISPLAY_VALUE));
		attributes.addElement("GivenName: " + (getGivenName() != null ? getGivenName() : NULL_DISPLAY_VALUE));
		attributes.addElement("Gender: " + (getGender() != null ? getGender() : NULL_DISPLAY_VALUE));
		attributes.addElement("BirthDate: " + (getBirthDate() != null ? Utilities.dateToString(getBirthDate()) : NULL_DISPLAY_VALUE));
	}
}
