package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

/** This object contains the collected data of a group in a form or questionaire
 * Separating group data and definition is for optimisation. This is achieved
 * by ensuring that one definition is maintained for each group type regardles
 * of how may groups of data have been collected.
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupData  implements Externalizable{
	
	/** The group number. */
	private byte groupNo = Constants.NULL_ID;
	
	/** A list of question data for this object. */
	private Vector questions;
	
	/** A reference to the group definition object. */
	private GroupDef def;
	
	public GroupData(){

	}
	
	/** Copy constructor. */
	public GroupData(GroupData data){
		setGroupNo(data.getGroupNo());
		copyQuestions(data.getQuestions());
		setDef(data.getDef());
	}

	public GroupData(Vector questions, GroupDef def) {
		this();
		setQuestions(questions);
		setDef(def);
		//setGroupNo(def.getGroupNo());
	}
	
	public Vector getQuestions() {
		return questions;
	}

	public void setQuestions(Vector questions) {
		this.questions = questions;
	}

	public byte getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(byte groupNo) {
		this.groupNo = groupNo;
	}
	
	public GroupDef getDef() {
		return def;
	}

	public void setDef(GroupDef def) {
		this.def = def;
	}
	
	private void copyQuestions(Vector qtns){
		if(qtns != null){
			questions  = new Vector();
			for(int i=0; i<qtns.size(); i++)
				questions.addElement(new QuestionData((QuestionData)qtns.elementAt(i)));
		}
	}

	/** Reads the group data object from the stream .*/
	public void readExternal(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		if(!ExternalizableHelper.isEOF(dis)){
			setGroupNo(dis.readByte());
			setQuestions(ExternalizableHelper.readExternal(dis,new QuestionData().getClass()));
		}
	}

	/** Writes the group data object to the stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
		dos.writeByte(getGroupNo());
		ExternalizableHelper.writeExternal(getQuestions(), dos);
	}
}
