package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;

/**
 * Definition of a form. This has some meta data about the form definition and  
 * a collection of pages together with question branching or skipping rules.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormDef implements Persistent{
	
	/** A collection of page definitions. */
	private Vector pages;
	
	/** The string unique identifier of the form definition. */
	private String variableName = EpihandyConstants.EMPTY_STRING;
	
	/** The display name of the form. */
	private String name = EpihandyConstants.EMPTY_STRING;
	
	/** The numeric unique identifier of the form definition. */
	private int id = EpihandyConstants.NULL_ID;
	
	/** The collection of rules for this form. */
	private Vector rules;
	
	/** A string constistig for form fields that describe its data. */
	private String descriptionTemplate =  EpihandyConstants.EMPTY_STRING;
  
	/** Constructs a form definition object. */
	public FormDef() {
		super();
	}
	
	/**
	 * Constructs a form definition object from these parameters.
	 * 
	 * @param name - the numeric unique identifier of the form definition.
	 * @param name - the display name of the form.
	 * @param variableName - the string unique identifier of the form definition.
	 * @param pages - collection of page definitions.
	 * @param rules - collection of branching rules.
	 */
	public FormDef(int id, String name, String variableName,Vector pages, Vector rules, String descTemplate) {
		this();
		setId(id);
		setName(name);
		setVariableName(variableName);
		setPages(pages);
		setRules(rules);
		setDescriptionTemplate((descTemplate == null) ? EpihandyConstants.EMPTY_STRING : descTemplate);
	}

	public Vector getPages() {
		return pages;
	}

	public void setPages(Vector pages) {
		this.pages = pages;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Vector getRules() {
		return rules;
	}

	public void setRules(Vector rules) {
		this.rules = rules;
	}

	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(String descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * Gets a question identified by a variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the question reference.
	 */
	public QuestionDef getQuestion(String varName){
		if(varName == null)
			return null;
		
		for(byte i=0; i<getPages().size(); i++){
			QuestionDef def = ((PageDef)getPages().elementAt(i)).getQuestion(varName);
			if(def != null)
				return def;
		}
		
		return null;
	}
	
	/**
	 * Gets a numeric question identifier for a given question variable name.
	 * 
	 * @param varName - the string identifier of the question. 
	 * @return the numeric question identifier.
	 */
	public byte getQuestionId(String varName){
		QuestionDef qtn = getQuestion(varName);
		if(qtn != null)
			return qtn.getId();
		
		return EpihandyConstants.NULL_ID;
	}

	public void addQuestion(QuestionDef qtn){
		if(pages == null){
			pages = new Vector();
			PageDef page = new PageDef(this.getVariableName(),Byte.parseByte("1"),null);
			pages.addElement(page);
		}
		
		((PageDef)pages.elementAt(0)).addQuestion(qtn);
	}
	
	/** 
	 * Reads the form definition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readInt());
			setName(dis.readUTF());
			setVariableName(dis.readUTF());
			setDescriptionTemplate(dis.readUTF());
			setPages(PersistentHelper.read(dis,new PageDef().getClass()));
			setRules(PersistentHelper.read(dis,new EpiHandySkipRule().getClass()));
		}
	}

	/** 
	 * Writes the form definition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getId());
		dos.writeUTF(getName());
		dos.writeUTF(getVariableName());
		dos.writeUTF(getDescriptionTemplate());
		PersistentHelper.write(getPages(), dos);
		PersistentHelper.write(getRules(), dos);
	}
}
