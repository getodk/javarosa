package org.javarosa.core.model.data;

import org.javarosa.core.util.externalizable.Externalizable;

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
	 * @param o the value of this answerdata object. Cannot be null.
	 * Null Data will not overwrite existing values.
	 * @throws NullPointerException if o is null
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
