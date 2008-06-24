package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * This class encapsulates all form definitions of a particular study.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyDef implements Persistent{
	private String variableName = EpihandyConstants.EMPTY_STRING;
	private String name = EpihandyConstants.EMPTY_STRING;
	
	//Assuming the number of studies will not exceed 127.
	private byte id = EpihandyConstants.NULL_ID;
	private Vector forms;
	
	/** Constructs a new study definitions. */
	public StudyDef() {
		super();
	}
	
	public StudyDef(byte id, String name, String variableName) {
		this();
		setId(id);
		setName(name);
		setVariableName(variableName);;
	}
	
	/** 
	 * Constructs a new study definition from the following parameters.
	 * 
	 * @param id - the numeric unique identifier of the study.
	 * @param name - the display name of the study.
	 * @param variableName - the text unique identifier of the study.
	 * @param forms - the collection of form definitions in the study.
	 */
	public StudyDef(byte id, String name, String variableName,Vector forms) {
		this(id,name,variableName);
		setForms(forms);
	}

	public Vector getForms() {
		return forms;
	}

	public void setForms(Vector forms) {
		this.forms = forms;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
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
	
	public FormDef getFormAt(byte index){
		return (FormDef)forms.elementAt(index);
	}
	
	public void addForm(FormDef formDef){
		if(forms == null)
			forms = new Vector();
		forms.addElement(formDef);
	}
	
	public void addForms(Vector formList){
		if(formList != null){
			if(forms == null)
				forms = formList;
			else{
				for(byte i=0; i<formList.size(); i++ )
					forms.addElement(formList.elementAt(i));
			}
		}
	}
	
	/** 
	 * Reads the study definition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readByte());
			setName(dis.readUTF());
			setVariableName(dis.readUTF());
			setForms(PersistentHelper.read(dis,new FormDef().getClass()));
		}
	}

	/** 
	 * Writes the study definition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeUTF(getName());
		dos.writeUTF(getVariableName());
		PersistentHelper.write(getForms(), dos);
	}
	
	/**
	 * Gets a form definition with a given string identifier.
	 * 
	 * @param varName - the string identifier.
	 * @return - the form definition.
	 */
	public FormDef getForm(String varName){
		for(byte i=0; i<forms.size(); i++){
			FormDef def = (FormDef)forms.elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
		}
		
		return null;
	}
	
	/**
	 * Gets a form definition with a given numeric identifier.
	 * 
	 * @param formId - the numeric identifier.
	 * @return - the form definition.
	 */
	public FormDef getForm(int formId){
		for(byte i=0; i<forms.size(); i++){
			FormDef def = (FormDef)forms.elementAt(i);
			if(def.getId() == formId)
				return def;
		}
		
		return null;
	}

	public String toString() {
		return getName();
	}
}
