package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.Restorable;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.patient.model.data.ImmunizationData;
import org.javarosa.patient.model.data.ImmunizationRow;
import org.javarosa.patient.model.data.NumericListData;

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
public class Patient implements Externalizable, IDRecordable, Restorable {
	public static final int SEX_MALE = 1;
	public static final int SEX_FEMALE = 2;
	public static final int SEX_UNKNOWN = 3;

	private static final int INVALID_RECORD_ID = -1;
	private static final String NULL_DISPLAY_VALUE = "";

	/** RMS Record Id */
	private int recordId = INVALID_RECORD_ID;
	
	String prefix;
	
	String familyName;  	/** Patient's Family name */
	String middleName;	    /** Optional Middle Name */
	String givenName;       /** Patient's surname */
	int gender;
	Date birthDate;
	boolean birthDateEstimated;
	
	Date treatmentStartDate;
	String treatmentRegimen;
	
	String patientIdentifier;
	boolean isNewPatient;
	

	/* String->NumericListData */
	private Hashtable records = new Hashtable();
	
	/* String->String */
	private Hashtable singles = new Hashtable();
	
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

	public int getAge () {
		return (int)(birthDate == null ? -1 : ((new Date()).getTime() - birthDate.getTime()) / 31556952000l);
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
	
	public String getInitials() {
		String s="";

		if(getGivenName() != null && getGivenName().length() > 0)
			s += getGivenName().charAt(0);
		
		if(getMiddleName() != null && getMiddleName().length() > 0)
			s += getMiddleName().charAt(0);
		
		if(getFamilyName() != null && getFamilyName().length() > 0)
			s += getFamilyName().charAt(0);

		return s.toUpperCase();
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
		} else if ("fullName".equals(recordType)) {
			return new StringData(getName());
		} else if ("ID".equals(recordType)) {
			return new StringData(patientIdentifier);
		} else {
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
	
	public void setRecord(String recordType, NumericListData record) {
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
			s = "NAMELESS PatientId="+getPatientIdentifier();
		return s;
	}
	
	
	public static final String ALIVE_KEY = "pat_al";
	private static final String ALIVE_YES = "a";
	private static final String ALIVE_NO = "d";
	public boolean isAlive() {
		String record = (String)this.singles.get(ALIVE_KEY);
		//Don't report dead patients unless there is a recorded
		//value that says they are
		if(ALIVE_NO.equals(record)) {
			return false;
		} else {
			return true;
		}
	}
	
	public void setAlive(boolean alive) {
		if(!alive) {
			singles.put(ALIVE_KEY,ALIVE_NO);
		} else {
			singles.put(ALIVE_KEY, ALIVE_YES);
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		recordId = ExtUtil.readInt(in);
		setPrefix((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setFamilyName((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setMiddleName((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setGivenName((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setGender(ExtUtil.readInt(in));
		setBirthDate((Date)ExtUtil.read(in, new ExtWrapNullable(Date.class), pf));
		setTreatmentStartDate((Date)ExtUtil.read(in, new ExtWrapNullable(Date.class), pf));
		setPatientIdentifier((String)ExtUtil.read(in, new ExtWrapNullable(String.class), pf));
		setNewPatient(ExtUtil.readBool(in));

		vaccinationData = (ImmunizationData)ExtUtil.read(in, ImmunizationData.class);
		
		records = (Hashtable)ExtUtil.read(in, new ExtWrapMap(String.class, NumericListData.class));
		singles = (Hashtable)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, recordId);
		ExtUtil.write(out, new ExtWrapNullable(getPrefix()));
		ExtUtil.write(out, new ExtWrapNullable(getFamilyName()));
		ExtUtil.write(out, new ExtWrapNullable(getMiddleName()));
		ExtUtil.write(out, new ExtWrapNullable(getGivenName()));
		ExtUtil.writeNumeric(out, getGender());
		ExtUtil.write(out, new ExtWrapNullable(getBirthDate()));
		ExtUtil.write(out, new ExtWrapNullable(getTreatmentStartDate()));
		ExtUtil.write(out, new ExtWrapNullable(getPatientIdentifier()));
		ExtUtil.writeBool(out, isNewPatient());
		
		ExtUtil.write(out, vaccinationData);
		
		ExtUtil.write(out, new ExtWrapMap(records));
		ExtUtil.write(out, new ExtWrapMap(singles));
	}
	
	public void setIdentifier(String identifier){
		setPatientIdentifier(identifier);
	}
	
	public String getIdentifier(){
		return getPatientIdentifier();
	}
		
	//NOTE: This class is still super-shoddy. The things below aren't really well written.
	//The system of attributes and individual values should be written, well, _better_.
	
	public void setValue(String type, String value) {
		singles.put(type, value);
	}
	
	public String getValue(String type) {
		return (String)singles.get(type);
	}
		
	public String getRestorableType () {
		return "patient";
	}
	
	public DataModelTree exportData() {
		DataModelTree dm = RestoreUtils.createDataModel(this);
		RestoreUtils.addData(dm, "pat-id", patientIdentifier);
		RestoreUtils.addData(dm, "name/family", familyName);
		RestoreUtils.addData(dm, "name/given", givenName);
		RestoreUtils.addData(dm, "name/middle", middleName);
		RestoreUtils.addData(dm, "name/prefix", prefix);
		RestoreUtils.addData(dm, "gender", new Integer(gender));
		RestoreUtils.addData(dm, "birthdate", birthDate);
		RestoreUtils.addData(dm, "birth-est", new Boolean(birthDateEstimated));
		
		for (Enumeration e = singles.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			RestoreUtils.addData(dm, "other/" + key, singles.get(key));
		}
				
		return dm;
	}
	
	public void templateData (DataModelTree dm, TreeReference parentRef) {
		RestoreUtils.applyDataType(dm, "pat-id", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "name/family", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "name/given", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "name/middle", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "name/prefix", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "gender", parentRef, Integer.class);
		RestoreUtils.applyDataType(dm, "birthdate", parentRef, Date.class);
		RestoreUtils.applyDataType(dm, "birth-est", parentRef, Boolean.class);
		
		// other/* defaults to string
	}
	
	public void importData(DataModelTree dm) {
		patientIdentifier = (String)RestoreUtils.getValue("pat-id", dm);
        familyName = (String)RestoreUtils.getValue("name/family", dm);		
        givenName = (String)RestoreUtils.getValue("name/given", dm);		
        middleName = (String)RestoreUtils.getValue("name/middle", dm);		
        prefix = (String)RestoreUtils.getValue("name/prefix", dm);		
        gender = ((Integer)RestoreUtils.getValue("gender", dm)).intValue();		
        birthDate = (Date)RestoreUtils.getValue("birthdate", dm);		
        birthDateEstimated = RestoreUtils.getBoolean(RestoreUtils.getValue("birth-est", dm));	
        
        TreeElement e = dm.resolveReference(RestoreUtils.absRef("other", dm));
        for (int i = 0; i < e.getNumChildren(); i++) {
        	TreeElement child = (TreeElement)e.getChildren().elementAt(i);
        	String name = child.getName();
        	singles.put(name, RestoreUtils.getValue("other/" + name, dm));
        }
	}
}
