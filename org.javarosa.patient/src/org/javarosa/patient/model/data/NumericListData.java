package org.javarosa.patient.model.data;

import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;

/**
 * @author Clayton Sims
 *
 */
public class NumericListData implements IAnswerData {

	Vector valueList;
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
	public String getDisplayText() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return valueList;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(Object o) { 
		valueList = (Vector)o;
	}

}
