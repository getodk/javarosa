package org.javarosa.core.model.data;

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
 * Note that this class currently maintains a reference
 * to a QuestionDef object in order to determine values
 * and strings. This is fairly hacky and should be refactored.
 * 
 * @author Drew Roos
 *
 */
public class Selection implements Externalizable {
	public int index;
	public QuestionDef question; //cannot hold reference directly to selectItems, as it is wiped out and rebuilt after every locale change
	
	/**
	 * Note that this constructor should only be used for serialization/deserialization as 
	 * the index and questiondef for a Selection shouldn't be changed after construction.
	 */
	public Selection() {
		
	}
	
	public Selection (int index, QuestionDef question) {
		this.index = index;
		this.question = question;
	}
	
	public String getText () {
		return (String)question.getSelectItems().keyAt(index);
	}
	
	public String getValue () {
		return (String)question.getSelectItems().elementAt(index);

		//NOTE:  Not sure whether this is actually correct, we definitely
		//should be returning what is in ItemIDs....
		//droos: it doesn't matter; the 'element' portions of both these hashtables should be identical
		// Clayton Sims - Sep 8, 2008 : At one point I was getting null pointers with that assumption.
		// It was when I was using something that had an [itext] value. 
		//return (String)question.getSelectItemsIDs().elementAt(index);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		index = ExtUtil.readInt(in);

		//setting QuestionDef in this way isn't correct; see note below
		question = (QuestionDef)ExtUtil.read(in, QuestionDef.class, pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	/* TODO: serializing the referenced QuestionDef directly isn't correct, and we're lucky it's not causing us
	 * problems right now. The point of keeping a reference to the QuestionDef is so that we have easy access
	 * to the localized text in the current locale. However, for this to work, the referenced QuestionDef must be
	 * the SAME QuestionDef that the FormDef uses. Serializing/deserializing the QuestionDef like we are now results
	 * in this object pointing to a DIFFERENT QuestionDef -- one that does not receive localization updates. (In fact,
	 * a QuestionDef that is never properly localized in the first place). We need to change this so that this object
	 * will point to the proper QuestionDef object.
	 * 
	 * The only reason this isn't biting us is because this Selection object will be discarded and rebuilt properly
	 * by the time we ever need to access the QuestionDef. We only call getText after the question has been
	 * skipped/answered, and when we answer/skip the select question, we create a new IAnswerData that overwrites this
	 * one. This invariance seems fragile, however.
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, index);
		
		//TODO: fix this
		ExtUtil.write(out, question);
	}
}
