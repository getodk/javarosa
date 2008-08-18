package org.javarosa.core.model.data;

import org.javarosa.core.util.Externalizable;

/**
 * An IAnswerData object represents an answer to a question
 * posed to a user.
 * 
 * IAnswerData objects should never in any circumstances contain
 * a null data value. In cases of empty or non-existent responses,
 * the IAnswerData reference should itself be null.
 *  
 * @author Drew Roos
 *
 */
public interface IAnswerData extends Externalizable {
	/**
	 * @param o the value of this answerdata object.
	 * o should not in any circumstances be null.
	 */
	void setValue (Object o); //can't be null
	/**
	 * @return The value of this answer, will never
	 * be null
	 */
	Object getValue ();       //will never be null
	/**
	 * @return Gets a string representation of this 
	 * answer
	 */
	String getDisplayText ();
}
