package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

public class Selection implements Externalizable {
	public int index;
	public QuestionDef question; //cannot hold reference directly to selectItems, as it is wiped out and rebuilt after every locale change
	
	/**
	 * Note that this constructor should only be used for serialization/deserialization as 
	 * selection is immutable
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
		//NOTE:  Not sure whether this is actually correct, we definitely
		//should be returning what is in ItemIDs....
		//droos: it doesn't matter; the 'element' portions of both these hashtables should be identical
		//return (String)question.getSelectItems().elementAt(index);
		return (String)question.getSelectItemIDs().elementAt(index);
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		index = in.readInt();
		
		//setting QuestionDef in this way isn't correct; see note below
		question = new QuestionDef();
		question.readExternal(in);
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
		out.writeInt(index);
		
		//TODO: fix this
		question.writeExternal(out); 
	}
}
