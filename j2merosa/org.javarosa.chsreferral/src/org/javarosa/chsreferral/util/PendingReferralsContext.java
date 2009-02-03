/**
 * 
 */
package org.javarosa.chsreferral.util;

import org.javarosa.core.Context;

/**
 * @author Clayton Sims
 * @date Feb 3, 2009 
 *
 */
public class PendingReferralsContext extends Context {
	private static final String PATIENT_ID = "PATIENT_ID";
	
	public PendingReferralsContext(Context context) {
		super(context);
	}
	
	public int getPatientId() {
		Integer id = (Integer)this.getElement(PATIENT_ID);
		if(id == null) {
			return -1;
		} else {
			return id.intValue();
		}
	}
	
	public void setPatientId(int id) {
		this.setElement(PATIENT_ID, new Integer(id));
	}
}
