package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * 
 * @author Daniel
 *
 */
public class OptionData  implements Persistent {
	
	private byte id = ModelConstants.NULL_ID;
	private OptionDef def;
	
	public OptionData(){
		super();  
	}

	/** Copy constructor. */
	public OptionData(OptionData data){
		setId(data.getId());
		setDef(data.getDef());
	}
	
	public OptionData( OptionDef def) {
		this();
		setDef(def);
		setId(def.getId());
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}
	
	public OptionDef getDef() {
		return def;
	}

	public void setDef(OptionDef def) {
		this.def = def;
	}

	public void read(DataInputStream dis) throws IOException {
		if(!PersistentHelper.isEOF(dis))
			setId(dis.readByte());
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
	}
	
	public String toString() {
		return getDef().getText();
	}
	
	public String getValue(){
		return getDef().getVariableName();
	}
}
