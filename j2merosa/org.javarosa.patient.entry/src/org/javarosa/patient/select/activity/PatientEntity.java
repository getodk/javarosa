package org.javarosa.patient.select.activity;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.patient.model.Patient;

public class PatientEntity implements IEntity {
	int recordID;
	String ID;	
	String familyName;
	String givenName;
	String middleName;
	int age;	
	int gender;
		
	String[] normalizedName;
	String normalizedID;
	
	public IEntity factory (int recordID) {
		PatientEntity pe = new PatientEntity();
		pe.recordID = recordID;
		return pe;
	}
	
	public String entityType() {
		return "Patient";
	}

	public void readEntity(Object o) {
		Patient p = (Patient)o;
					
		ID = ExtUtil.emptyIfNull(p.getIdentifier());
		familyName = ExtUtil.emptyIfNull(p.getFamilyName());
		givenName = ExtUtil.emptyIfNull(p.getGivenName());
		middleName = ExtUtil.emptyIfNull(p.getMiddleName());
		age = p.getAge();
		gender = p.getGender();
		
		normalizedName = normalizeNames();
		normalizedID = normalizeID(getID());
	}

	public Object fetchRMS (RMSUtility rmsu) {
		Patient p = new Patient();
		try {
			rmsu.retrieveFromRMS(recordID, p);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializationException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public String getID() {
		return ID == null ? "" : ID;
	}

	public String getName() {
		String name = "";
		
		if (familyName != null) {
			name += familyName;
		}
		
		if (givenName != null) {
			if (name.length() > 0) {
				name += ", ";
			}
			name += givenName;
		}
		
		if (middleName != null) {
			if (name.length() > 0) {
				name += " ";
			}
			name += middleName;
		}
		
		return name;
	}

	public int getRecordID() {
		return recordID;
	}
	
	private static String normalizeID (String ID) {
		StringBuffer sb = new StringBuffer();
		char[] carr = ID.toCharArray();
		
		for (int i = 0; i < carr.length; i++) {
			char c = carr[i];
			if (Character.isDigit(c) || Character.isLowerCase(c) || Character.isUpperCase(c)) {
				if (Character.isLowerCase(c))
					c = Character.toUpperCase(c);
				sb.append(c);
			}
		}
			
		return sb.toString();
	}
	
	private static Vector normalizeName (String name) {
		StringBuffer sb = new StringBuffer();
		char[] carr = name.toCharArray();
		
		for (int i = 0; i < carr.length; i++) {
			char c = carr[i];
			if (Character.isDigit(c) || Character.isLowerCase(c) || Character.isUpperCase(c)) {
				if (Character.isLowerCase(c))
					c = Character.toUpperCase(c);
				sb.append(c);
		//	} else if (c == '\'' || c == '-') {
		//		//do nothing
			} else {
				sb.append(' ');
			}
		}
			
		return DateUtils.split(sb.toString(), " ", true);
	}
	
	private static boolean findKey (String str, String key, boolean anywhere) {
		if (str.length() == 0 || key.length() == 0) {
			return false;
		} else {
			if (anywhere) {
				return (str.indexOf(key) != -1);
			} else {
				return str.startsWith(key);
			}
		}
	}
	
	public boolean matchID(String key) {
		String normalizedKey = normalizeID(key);
		
		return findKey(normalizedID, normalizedKey, false);
	}

	private void concatVector (Vector base, Vector append) {
		for (int i = 0; i < append.size(); i++)
			base.addElement(append.elementAt(i));
	}
	
	private String[] normalizeNames () {
		Vector nameFrags = new Vector();
		concatVector(nameFrags, normalizeName(familyName));
		concatVector(nameFrags, normalizeName(givenName));
		concatVector(nameFrags, normalizeName(middleName));
		
		String[] nameNorm = new String[nameFrags.size()];
		for (int i = 0; i < nameNorm.length; i++)
			nameNorm[i] = (String)nameFrags.elementAt(i);
		return nameNorm;
	}
	
	public boolean matchName(String key) {
		Vector keyFrags = normalizeName(key);
		
		for (int i = 0; i < keyFrags.size(); i++) {
			String keyFrag = (String)keyFrags.elementAt(i);
			boolean fragMatched = false;
			for (int j = 0; j < normalizedName.length; j++) {
				String nameFrag = normalizedName[j];
				if (findKey(nameFrag, keyFrag, false)) {
					fragMatched = true;
					break;
				}
			}
			if (!fragMatched) {
				return false;
			}
		}
		return true;
	}

	public String[] getHeaders(boolean detailed) {
		//#if javarosa.patientselect.formfactor == nokia-s40
		String[] shortHeaders = {"Name", "ID"};
		//#else
		String[] shortHeaders = {"Name", "ID", "Age/Sex"};
		//#endif

		String[] longHeaders = {"Name", "ID", "Sex", "DOB", "Age", "Phone", "Village"};
		
		return detailed ? longHeaders : shortHeaders;
	}

	public String[] getShortFields() {
		String[] fields = new String[getHeaders(false).length];
		fields[0] = getName();
		fields[1] = getID();
		
		//#if javarosa.patientselect.formfactor != nokia-s40
		
		String sexStr;
		switch (gender) {
		case Patient.SEX_MALE: sexStr = "M"; break;
		case Patient.SEX_FEMALE: sexStr = "F"; break;
		default: sexStr = "?"; break;
		}
		
		fields[2] = (age == -1 ? "?" : age + "") + "/" + sexStr;

		//#endif
		
		return fields;
	}

	public String[] getLongFields(Object o) {
		Patient p = (Patient)o;

		String[] fields = new String[getHeaders(true).length];
	
		String sexStr;
		switch (gender) {
		case Patient.SEX_MALE: sexStr = "Male"; break;
		case Patient.SEX_FEMALE: sexStr = "Female"; break;
		default: sexStr = "Unknown"; break;
		}
		
		Random r = new Random();
		
		String village = null;
		switch(r.nextInt(10)) {
		case 0: village = "Mikocheni"; break;
		case 1: village = "Bagamoyo"; break;
		case 2: village = "Mbezi"; break;
		case 3: village = "Kariakoo"; break;
		case 4: village = "Msasani"; break;
		case 5: village = "Kinondoni"; break;
		case 6: village = "Tabora"; break;
		case 7: village = "Kigoma"; break;
		case 8: village = "Ifakara"; break;
		case 9: village = "Kigomboni"; break;
		}
		
		String phone = "07";
		for (int i = 0; i < 8; i++)
			phone += (r.nextInt(10));
		
		fields[0] = getName();
		fields[1] = getID();
		fields[2] = sexStr;
		fields[3] = DateUtils.formatDate(p.getBirthDate(), DateUtils.FORMAT_HUMAN_READABLE_SHORT);
		fields[4] = (age == -1 ? "" : age + "");
		fields[5] = phone;
		fields[6] = village;
		
		return fields;
	}
}
