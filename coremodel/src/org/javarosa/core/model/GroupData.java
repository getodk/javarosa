package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;

/** This object contains the collected data of a group in a form or questionaire
 * Separating group data and definition is for optimisation. This is achieved
 * by ensuring that one definition is maintained for each group type regardles
 * of how may groups of data have been collected.
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupData  implements Persistent{
	
	/** The group number. */
	private byte groupNo = ModelConstants.NULL_ID;
	
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
		setGroupNo(def.getGroupNo());
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
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setGroupNo(dis.readByte());
			setQuestions(PersistentHelper.read(dis,new QuestionData().getClass()));
		}
	}

	/** Writes the group data object to the stream. */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getGroupNo());
		PersistentHelper.write(getQuestions(), dos);
	}
}
