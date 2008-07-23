/**
 * 
 */
package org.javarosa.core.model.data;

/**
 * @author Clayton Sims
 *
 */
public class IntegerData implements IAnswerData {
	Integer data;

	public IntegerData(int i) {
		setValue(i);
	}
	
	public IntegerData(Integer o) { 
		setValue(o);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
	public String getDisplayText() {
		return String.valueOf(data);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return data; 
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(Object o) {
		data = (Integer)o;
	}
	
	public void setValue(int i) {
		data = new Integer(i);
	}

}
