package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.util.db.*;


/**
 * This calls encapsulates data collected in all forms of a particular study.
 * 
 * @author Daniel Kayiwa
 *
 */
public class StudyData implements Persistent{
	private byte id = EpihandyConstants.NULL_ID; //this is just for storage;
	private StudyDef def;
	private Vector forms;
	
	/** Creates a new study data object. */
	public StudyData(){
		super();
	}
	
	/**
	 * Creates a new study data object form these parameters.
	 * 
	 * @param id - the id of the study definition represented by this data.
	 */
	public StudyData(byte id) {
		this();
		setId(id);
	}
	
	/**
	 * Creates a new study data object form these parameters.
	 * 
	 * @param def - reference to the study definition represented by this data.
	 */
	public StudyData(StudyDef def) {
		this();
		setDef(def);
		setId(def.getId());
	}

	public StudyDef getDef() {
		return def;
	}

	public void setDef(StudyDef def) {
		this.def = def;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}
	
	public Vector getForms() {
		return forms;
	}

	public void setForms(Vector forms) {
		this.forms = forms;
	}
	
	public void addForm(FormData formData){
		if(forms == null)
			forms = new Vector();
		forms.addElement(formData);
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
	 * Reads the study data object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readByte());
			setForms(PersistentHelper.read(dis,new FormData().getClass()));
		}
	}

	/** 
	 * Writes the study data object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		PersistentHelper.write(getForms(), dos);
	}
}
