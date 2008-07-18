package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;


/** The definition of a group in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements Externalizable{
	
	/** A list of questions on a group. */
	private Vector questions;
	
	/** The group number. */
	private byte groupNo = Constants.NULL_ID;
	
	/** The name of the group. */
	private String name = Constants.EMPTY_STRING;
	
	public GroupDef() {
		 
	}
	
	public GroupDef(String name, byte groupNo,Vector questions) {
		this();
		setName(name);
		setGroupNo(groupNo);
		setQuestions(questions);
	}
	
	public byte getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(byte groupNo) {
		this.groupNo = groupNo;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector getQuestions() {
		return questions;
	}

	public void addQuestion(QuestionDef qtn){
		if(questions == null)
			questions = new Vector();
		questions.addElement(qtn);
	}
	
	public void setQuestions(Vector questions) {
		this.questions = questions;
	}
	
	public QuestionDef getQuestion(String varName){
		for(byte i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
		}
		
		return null;
	}
	
	public QuestionDef getQuestionById(String id){
		for(byte i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getId() == id)
				return def;
		}
		
		return null;
	}

	/** Reads a group definition object from the supplied stream. */
	public void readExternal(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		if(!ExternalizableHelper.isEOF(dis)){
			setGroupNo(dis.readByte());
			setName(dis.readUTF());
			setQuestions(ExternalizableHelper.readExternal(dis,new QuestionDef().getClass()));
		}
	}

	/** Write the group definition object to the supplied stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
		dos.writeByte(getGroupNo());
		dos.writeUTF(getName());
		ExternalizableHelper.writeExternal(getQuestions(), dos);
	}
	
	public String toString() {
		return getName();
	}
}
