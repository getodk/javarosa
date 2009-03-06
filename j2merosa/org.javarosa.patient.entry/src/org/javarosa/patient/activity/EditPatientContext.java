/**
 * 
 */
package org.javarosa.patient.activity;

import org.javarosa.core.Context;

/**
 * @author Clayton Sims
 * @date Mar 6, 2009 
 *
 */
public class EditPatientContext extends Context {
	
	private static final String PATIENT_ID = "pat_id";
	
	public EditPatientContext(Context c) {
		super(c);
	}
	
	public void setPatientId(int id) {
		this.setElement(PATIENT_ID, new Integer(id));
	}
	
	public int getPatientId() {
		Integer id = (Integer)this.getElement(PATIENT_ID);
		if(id == null) {
			return -1;
		} else {
			return id.intValue();
		}
	}

}
