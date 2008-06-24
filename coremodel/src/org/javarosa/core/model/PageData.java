package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;

/** This object contains the collected data of a page in a form or questionaire
 * Separating page data and definition is for optimisation. This is achieved
 * by ensuring that one definition is maintained for each page type regardles
 * of how may pages of data have been collected.
 * 
 * @author Daniel Kayiwa
 *
 */
public class PageData  implements Persistent{
	
	/** The page number. */
	private byte pageNo = EpihandyConstants.NULL_ID;
	
	/** A list of question data for this object. */
	private Vector questions;
	
	/** A reference to the page definition object. */
	private PageDef def;
	
	public PageData(){

	}
	
	/** Copy constructor. */
	public PageData(PageData data){
		setPageNo(data.getPageNo());
		copyQuestions(data.getQuestions());
		setDef(data.getDef());
	}

	public PageData(Vector questions, PageDef def) {
		this();
		setQuestions(questions);
		setDef(def);
		setPageNo(def.getPageNo());
	}
	
	public Vector getQuestions() {
		return questions;
	}

	public void setQuestions(Vector questions) {
		this.questions = questions;
	}

	public byte getPageNo() {
		return pageNo;
	}

	public void setPageNo(byte pageNo) {
		this.pageNo = pageNo;
	}
	
	public PageDef getDef() {
		return def;
	}

	public void setDef(PageDef def) {
		this.def = def;
	}
	
	private void copyQuestions(Vector qtns){
		if(qtns != null){
			questions  = new Vector();
			for(int i=0; i<qtns.size(); i++)
				questions.addElement(new QuestionData((QuestionData)qtns.elementAt(i)));
		}
	}

	/** Reads the page data object from the stream .*/
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setPageNo(dis.readByte());
			setQuestions(PersistentHelper.read(dis,new QuestionData().getClass()));
		}
	}

	/** Writes the page data object to the stream. */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getPageNo());
		PersistentHelper.write(getQuestions(), dos);
	}
}
