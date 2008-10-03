package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.StudyDef;
import org.javarosa.core.model.storage.FormDefMetaData;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * This calls encapsulates data collected in all forms of a particular study.
 * 
 * @author Mark Gerard
 *
 */
public class StudyData implements Externalizable{

	private byte id = Constants.NULL_ID; //this is just for storage;
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
	
	public void addForm(FormDefMetaData formData){
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
	 * Reads the study data object from the stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		if(!ExternalizableHelperDeprecated.isEOF(in)){
			setId(in.readByte());
			setForms(ExternalizableHelperDeprecated.readBig(in,new FormDefMetaData().getClass()));
		}
		
	}

	/** 
	 * Writes the study collection object to the stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeByte(getId());
		ExternalizableHelperDeprecated.writeBig(getForms(), out);
		
	}
}
