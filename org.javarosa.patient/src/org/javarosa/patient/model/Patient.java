package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.Map;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.patient.model.data.ImmunizationData;
import org.javarosa.patient.model.data.ImmunizationRow;
import org.javarosa.patient.model.data.NumericListData;
import org.javarosa.util.Utilities;

/**
 * Encapsulates demographic data about a patient and contains 
 * information and records for a single patient. These include 
 * static data types such as a patient's id and name, and also 
 * historical data records for patients, such as a list of weight
 * measurements . 
 * 
 * @author daniel/ctsims(temp)
 *
 */
public class Patient implements Externalizable, IDRecordable {
	public static final int SEX_MALE = 1;
	public static final int SEX_FEMALE = 2;
	public static final int SEX_UNKNOWN = 3;

	private static final int INVALID_RECORD_ID = -1;
	private static final String NULL_DISPLAY_VALUE = "";

	/** RMS Record Id */
	private int recordId = INVALID_RECORD_ID;
	
	/** Patient's medical record ID. Is made Integer instead of int because new patients dont have it. */
	Integer patientId; //patientid 
	String prefix;
	
	String familyName;  	/** Patient's Family name */
	String middleName;	    /** Optional Middle Name */
	String givenName;       /** Patient's surname */
	int gender;
	Date birthDate;
	boolean birthDateEstimated;
	int age;
	
	Date treatmentStartDate;
	String treatmentRegimen;
	
	String patientIdentifier;
	Vector attributes; //Not serialized since we can generate it on the fly.
	boolean isNewPatient;
	

	/* String->NumericListData */
	private Map records = new Map();
	
	//For now we're statically encoding record types.
	private NumericListData weightRecord = new NumericListData();
	
	private NumericListData heightRecord = new NumericListData();
	
	private NumericListData cd4CountRecord = new NumericListData();
	
	ImmunizationData vaccinationData;
	
	public Patient() { 
		records.put("weight", weightRecord);
		records.put("height", heightRecord);
		records.put("cd4count", cd4CountRecord);
		
		Vector rows = new Vector();
		ImmunizationRow bcg = new ImmunizationRow("BCG");
		bcg.setCellEnabled(1, false);
		bcg.setCellEnabled(2, false);
		bcg.setCellEnabled(3, false);
		bcg.setCellEnabled(4, false);
		rows.addElement(bcg);
		ImmunizationRow opv = new ImmunizationRow("OPV");
		opv.setCellEnabled(4, false);
		rows.addElement(opv);
		ImmunizationRow dpt = new ImmunizationRow("DPT");
		dpt.setCellEnabled(0, false);
		dpt.setCellEnabled(4, false);
		rows.addElement(dpt);
		ImmunizationRow hepb = new ImmunizationRow("Hep B");
		hepb.setCellEnabled(0, false);
		hepb.setCellEnabled(4, false);
		rows.addElement(hepb);
		ImmunizationRow measles = new ImmunizationRow("Measles");
		measles.setCellEnabled(0,false);
		measles.setCellEnabled(1,false);
		measles.setCellEnabled(2,false);
		measles.setCellEnabled(3,false);
		rows.addElement(measles);
		vaccinationData = new ImmunizationData(rows);
		
		//records.put("vaccinations", vaccinationData);
	}
	
	/**
	 * Gets the patient whole name which is a concatenation of the
	 * given, middle and family names.
	 * 
	 * @return
	 */
	public String getName (){
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
	public int getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(int gender) {
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
	
	public void setVaccinations(ImmunizationData data ){
		this.vaccinationData = data;
	}
	
	public ImmunizationData getVaccinations() {
		return this.vaccinationData;
	}

	public IAnswerData getRecord(String recordType) {
		if(recordType == "givenName") {
			return new StringData(givenName);
		} else if (recordType == "sex") {
			switch (gender) {
			case SEX_MALE: return new StringData("m");
			case SEX_FEMALE: return new StringData("f");
			default: return null;
			}
		} else if (recordType == "age") {
			return new IntegerData((int)(((new Date()).getTime() - birthDate.getTime()) / 31556952000l));
		} else if (recordType == "treatmentStart") {
			if(this.treatmentStartDate != null) {
				return new DateData(this.treatmentStartDate);
			}
			else {
				return null;
			}
		} else if (recordType == "treatmentregimen") {
			if(getTreatmentRegimen() != null) {
				return new StringData(getTreatmentRegimen());
			} else {
				return null;
			}
		}else {
			//TODO: We need to figure out how to do these references better
			return null;
		}
	}
	
	public void setRecord (String recordType, Object o) {
		//not needed right now
		
		
	}
	
	public Vector getRecordSet(String recordType, String selector) {
		IPatientRecord record = (IPatientRecord)records.get(recordType);
		if(record == null) {
			return new Vector();
		}
		return record.getHistoricalRecords(selector);
	}
	
	public void setRecord(String recordType, IPatientRecord record) {
		records.put(recordType, record);
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
	
	/**
	 * @return the treatmentStartDate
	 */
	public Date getTreatmentStartDate() {
		return treatmentStartDate;
	}

	/**
	 * @param treatmentStartDate the treatmentStartDate to set
	 */
	public void setTreatmentStartDate(Date treatmentStartDate) {
		this.treatmentStartDate = treatmentStartDate;
	}

	/**
	 * @return the treatmentRegimen
	 */
	public String getTreatmentRegimen() {
		return treatmentRegimen;
	}

	/**
	 * @param treatmentRegimen the treatmentRegimen to set
	 */
	public void setTreatmentRegimen(String treatmentRegimen) {
		this.treatmentRegimen = treatmentRegimen;
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

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		recordId = in.readInt();
	
		setPatientId(ExternalizableHelperDeprecated.readInteger(in));
		setPrefix(ExternalizableHelperDeprecated.readUTF(in));
		setFamilyName(ExternalizableHelperDeprecated.readUTF(in));
		setMiddleName(ExternalizableHelperDeprecated.readUTF(in));
		setGivenName(ExternalizableHelperDeprecated.readUTF(in));
		setGender(ExternalizableHelperDeprecated.readNumInt(in, ExternalizableHelperDeprecated.ENCODING_NUM_DEFAULT));
		setBirthDate(ExternalizableHelperDeprecated.readDate(in));
		setTreatmentStartDate(ExternalizableHelperDeprecated.readDate(in));
		setPatientIdentifier(ExternalizableHelperDeprecated.readUTF(in));
		setNewPatient(in.readBoolean());

		vaccinationData = new ImmunizationData();
		vaccinationData.readExternal(in, pf);
		
		records = ExternalizableHelperDeprecated.readExternalStringValueMap(in, NumericListData.class);		
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordId);
			
		ExternalizableHelperDeprecated.writeInteger(out,getPatientId());
		ExternalizableHelperDeprecated.writeUTF(out, getPrefix());
		ExternalizableHelperDeprecated.writeUTF(out, getFamilyName());
		ExternalizableHelperDeprecated.writeUTF(out, getMiddleName());
		ExternalizableHelperDeprecated.writeUTF(out, getGivenName());
		ExternalizableHelperDeprecated.writeNumeric(out, getGender(), ExternalizableHelperDeprecated.ENCODING_NUM_DEFAULT);
		ExternalizableHelperDeprecated.writeDate(out, getBirthDate());
		ExternalizableHelperDeprecated.writeDate(out, getTreatmentStartDate());
		ExternalizableHelperDeprecated.writeUTF(out, getPatientIdentifier());
		out.writeBoolean(isNewPatient());
		
		vaccinationData.writeExternal(out);
		
		ExternalizableHelperDeprecated.writeExternalStringValueMap(out, records);
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
	
//	commented out until entitymgr compiles	
//	public void addAttributes(EntityFieldList fields, EntityFieldValueList fieldVals){
//		if(attributes == null)
//			setDefaultAttributes();
//		
//		/*for(int i=0; i<fields.size(); i++){
//			EntityField field = fields.getField(i);
//			if(data.containsQuestion(field.getName()))
//				data.setValue(field.getName(), fieldVals.getPatintFiledValue(field.getId(), getPatientId()));
//		}
//		
//		attributes.addElement(attribute);*/
//	}
	
	private void setDefaultAttributes(){
		attributes = new Vector();
		attributes.addElement("Identifier: " + (getPatientIdentifier() != null ? getPatientIdentifier() : NULL_DISPLAY_VALUE));
		attributes.addElement("Prefix: " + (getPrefix() != null ? getPrefix() : NULL_DISPLAY_VALUE));
		attributes.addElement("FamilyName: " + (getFamilyName() != null ? getFamilyName() : NULL_DISPLAY_VALUE));
		attributes.addElement("MiddleName: " + (getMiddleName() != null ? getMiddleName() : NULL_DISPLAY_VALUE));
		attributes.addElement("GivenName: " + (getGivenName() != null ? getGivenName() : NULL_DISPLAY_VALUE));
		attributes.addElement("Gender: " + (getGender() == SEX_MALE ? "Male" : getGender() == SEX_FEMALE ? "Female" : NULL_DISPLAY_VALUE));
		attributes.addElement("BirthDate: " + (getBirthDate() != null ? Utilities.dateToString(getBirthDate()) : NULL_DISPLAY_VALUE));
	}
}
