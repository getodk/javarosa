package org.javarosa.core.model.data.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a selection
 * from a list. 
 * 
 * @author Drew Roos
 *
 */
public class Selection implements Externalizable {
	public int index;
	
	/* we need the questiondef to fetch natural-language captions for the selected choice
	 * we can't hold a reference directly to the caption hashtable, as it's wiped out and
	 * recreated every locale change
	 * we don't serialize the questiondef, as it's huge, and unneeded outside of a formdef;
	 * it is restored as a post-processing step during formdef deserialization
	 */
	public QuestionDef question; 
	
	public int qID = -1;
	public String xmlValue = null;
	
	/**
	 * for deserialization
	 */
	public Selection() {
		
	}
	
	public Selection (int index, QuestionDef question) {
		this.index = index;
		this.question = question;
		
		if (question != null) {
			//don't think setting these is strictly necessary, setting them only on deserialization is probably enough
			this.qID = question.getID();
			this.xmlValue = getValue();
		} //if question is null, these had better be set manually afterward!
	}
	
	public Selection clone () {
		Selection s = new Selection(index, question);
		
		//don't think setting these is strictly necessary, question should always be set by the time clone() is called
		//on second thought, this might not be such a safe assumption
		s.qID = qID;
		s.xmlValue = xmlValue;
		
		return s;
	}
	
	public String getText () {
		if (question != null) {
			return (String)question.getSelectItems().keyAt(index);
		} else {
			System.err.println("Warning!! Calling Selection.getText() when QuestionDef not set!");
			return "[cannot access choice caption]";
		}
	}
	
	public String getValue () {
		if (question != null) {
			return (String)question.getSelectItems().elementAt(index);
		} else {
			return xmlValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		index = ExtUtil.readInt(in);
		
		qID = ExtUtil.readInt(in);
		xmlValue = ExtUtil.readString(in);
	}
 
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, index);
		
		ExtUtil.writeNumeric(out, question != null ? question.getID() : qID);
		ExtUtil.writeString(out, getValue());
	}
}
