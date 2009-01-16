package org.javarosa.patient.select.activity;

import java.io.IOException;
import java.util.Date;
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
		String IDn = normalizeID(getID());
		String keyn = normalizeID(key);
		
		System.out.println(IDn + " " + keyn);

		return findKey(IDn, keyn, false);
	}

	private void concatVector (Vector base, Vector append) {
		for (int i = 0; i < append.size(); i++)
			base.addElement(append.elementAt(i));
	}
	
	public boolean matchName(String key) {
		Vector nameFrags = new Vector();
		concatVector(nameFrags, normalizeName(familyName));
		concatVector(nameFrags, normalizeName(givenName));
		concatVector(nameFrags, normalizeName(middleName));

		Vector keyFrags = normalizeName(key);
		
		System.out.println(nameFrags + " " + keyFrags);
		
		for (int i = 0; i < keyFrags.size(); i++) {
			String keyFrag = (String)keyFrags.elementAt(i);
			boolean fragMatched = false;
			for (int j = 0; j < nameFrags.size(); j++) {
				String nameFrag = (String)nameFrags.elementAt(j);
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
		//#if polish.ScreenWidth > 128 || device.identifier == 'Generic/DefaultColorPhone'
		String[] shortHeaders = {"Name", "ID", "Age/Sex"};
		//#else
		String[] shortHeaders = {"Name", "ID"};
		//#endif

		String[] longHeaders = {"Name", "ID", "Sex", "DOB", "Age"};
		
		return detailed ? longHeaders : shortHeaders;
	}

	public String[] getShortFields() {
		String[] fields = new String[getHeaders(false).length];
		fields[0] = getName();
		fields[1] = getID();
		
		//#if polish.ScreenWidth > 128 || device.identifier == 'Generic/DefaultColorPhone'

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

		String[] fields = new String[5];
	
		String sexStr;
		switch (gender) {
		case Patient.SEX_MALE: sexStr = "Male"; break;
		case Patient.SEX_FEMALE: sexStr = "Female"; break;
		default: sexStr = "Unknown"; break;
		}
		
		fields[0] = getName();
		fields[1] = getID();
		fields[2] = sexStr;
		fields[3] = DateUtils.formatDate(p.getBirthDate(), DateUtils.FORMAT_HUMAN_READABLE_SHORT);
		fields[4] = (age == -1 ? "" : age + "");
		
		return fields;
	}
}
