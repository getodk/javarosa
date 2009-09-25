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

package org.javarosa.patient.select.activity;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.entity.model.Entity;
import org.javarosa.patient.model.Patient;

public class PatientEntity implements Entity {
	protected int recordID;
	protected String ID;	
	protected String familyName;
	protected String givenName;
	protected String middleName;
	protected int age;	
	protected int gender;
	
	protected boolean alive;
		
	protected String[] normalizedName;
	protected String normalizedID;
	
	public Entity factory (int recordID) {
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
		
		alive = p.isAlive();
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
		
		if ((givenName!=null) && (givenName.length() > 0)) {
			if (name.length() > 0) {
				name += ", ";
			}
			name += givenName;
		}
		
		if ((middleName !=null) && (middleName.length() > 0)) {
			if (name.length() > 0) {
				name += " ";
			}
			name += middleName;
		}
		
		if ((familyName !=null) && (familyName.length() > 0)) {
			if (name.length() > 0) {
				name += " ";
			}
			name += familyName;
		}
		
		return name;
	}

	public int getRecordID() {
		return recordID;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	protected static String normalizeID (String ID) {
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
	
	protected static Vector normalizeName (String name) {
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
	
	protected static boolean findKey (String str, String key, boolean anywhere) {
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

	protected void concatVector (Vector base, Vector append) {
		for (int i = 0; i < append.size(); i++)
			base.addElement(append.elementAt(i));
	}
	
	protected String[] normalizeNames () {
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
		//# String[] shortHeaders = {"Name", "ID"};
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
