package org.javarosa.patient.select.activity;

import org.javarosa.patient.model.Patient;

public class CommCarePatientEntity extends PatientEntity {
	public IEntity factory (int recordID) {
		CommCarePatientEntity ccpe = new CommCarePatientEntity();
		ccpe.recordID = recordID;
		return ccpe;
	}
	
	public String entityType() {
		return "Client";
	}
		
	protected String getShortID () {
		String ID = getID();
		return ID.substring(ID.length() - 2);
	}

	protected String getInitials () {
		String initials = "";

		initials += (givenName.length() > 0 ? givenName.substring(0, 1) : ".");
		initials += (middleName.length() > 0 ? middleName.substring(0, 1) : "");
		initials += (familyName.length() > 0 ? familyName.substring(0, 1) : ".");
				
		return initials;
	}
	
	public boolean matchID(String key) {
		String normalizedKey = normalizeID(key);
		
		return findKey(normalizeID(getShortID()), normalizedKey, false);
	}

	public String[] getHeaders(boolean detailed) {
		String[] shortHeaders = {"ID", "Initials", "Age/Sex"};
		String[] longHeaders = {"Name", "ID", "Sex", "DOB", "Age", "Phone", "Village"};
		
		return detailed ? longHeaders : shortHeaders;
	}

	public String[] getShortFields() {
		String[] fields = new String[getHeaders(false).length];
		fields[0] = getInitials();
		fields[1] = "...-" + getShortID();
		
		String sexStr;
		switch (gender) {
		case Patient.SEX_MALE: sexStr = "M"; break;
		case Patient.SEX_FEMALE: sexStr = "F"; break;
		default: sexStr = "?"; break;
		}

		fields[2] = (age == -1 ? "?" : age + "") + "/" + sexStr;

		return fields;
	}
}
