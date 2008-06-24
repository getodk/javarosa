package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/** The definition of a page in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements Persistent{
	
	/** A list of questions on a page. */
	private Vector questions;
	
	/** The page number. */
	private byte pageNo = EpihandyConstants.NULL_ID;
	
	/** The name of the page. */
	private String name = EpihandyConstants.EMPTY_STRING;
	
	public GroupDef() {
		 
	}
	
	public GroupDef(String name, byte pageNo,Vector questions) {
		this();
		setName(name);
		setPageNo(pageNo);
		setQuestions(questions);
	}
	
	public byte getPageNo() {
		return pageNo;
	}

	public void setPageNo(byte pageNo) {
		this.pageNo = pageNo;
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
	
	public QuestionDef getQuestion(byte id){
		for(byte i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getId() == id)
				return def;
		}
		
		return null;
	}

	/** Reads a page definition object from the supplied stream. */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setPageNo(dis.readByte());
			setName(dis.readUTF());
			setQuestions(PersistentHelper.read(dis,new QuestionDef().getClass()));
		}
	}

	/** Write the page definition object to the supplied stream. */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getPageNo());
		dos.writeUTF(getName());
		PersistentHelper.write(getQuestions(), dos);
	}
	
	public String toString() {
		return getName();
	}
}
