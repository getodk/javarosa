/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.core.Context;

/**
 * @author ctsims
 *
 */
public class CaseContext extends Context {
	
	public CaseContext(Context c){
		super(c);
	}
	
	public static final String NUM_REFS = "cc_numref";
	
	public int getNumberOfReferrals() {
		Integer refs = (Integer)getElement(NUM_REFS);
		if(refs == null) {
			return -1;
		}
		return refs.intValue();
	}
	
	public void setNumberOfReferrals(int refs) {
		setElement(NUM_REFS, new Integer(refs));
	}

}
