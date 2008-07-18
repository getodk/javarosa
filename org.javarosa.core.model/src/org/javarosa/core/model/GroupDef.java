package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.*;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;


/** The definition of a group in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements IFormElement, Localizable, Externalizable{
	private Vector children;	/** A list of questions on a group. */	
	private boolean repeat;
	private int id;	/** The group number. */
	private String name;	/** The name of the group. */
	
	private String longText;
	private String longTextID;
	private String shortText;
	private String shortTextID;
	
	public GroupDef () {
		this(Constants.NULL_ID, null, null, false);
	}
	
	public GroupDef(int id, String name, Vector children, boolean repeat) {
		setName(name);
		setID(id);
		setChildren(children);
		setRepeat(repeat);
	}
	
	public int getID () {
		return id;
	}
	
	public void setID (int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector getChildren() {
		return children;
	}

	public void setChildren (Vector children) {
		this.children = (children == null ? new Vector() : children);
	}
	
	public void addChild (IFormElement fe) {
		children.addElement(fe);
	}
	
	public boolean getRepeat () {
		return repeat;
	}
	
	public void setRepeat (boolean repeat) {
		this.repeat = repeat;
	}
	
	public String getLongText () {
		return longText;
	}
	
	public void setLongText (String longText) {
		this.longText = longText;
	}

    public String getLongTextID () {
        return longTextID;
    }
    
    public void setLongTextID (String textID, Localizer localizer) {
    	this.longTextID = textID;
    	if (localizer != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}
    }
	
	public String getShortText () {
		return shortText;
	}
	
	public void setShortText (String shortText) {
		this.shortText = shortText;
	}

    public String getShortTextID () {
        return shortTextID;
    }

    public void setShortTextID (String textID, Localizer localizer) {
    	this.shortTextID = textID;
    	if (localizer != null) {
    		shortText = localizer.getLocalizedText(shortTextID);        
    	}
    } 
	
	/*
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
	*/
    
    public void localeChanged(String locale, Localizer localizer) {
    	if(longTextID != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}

    	if(shortTextID != null) {
    		shortText = localizer.getLocalizedText(shortTextID);
    	}
    }
	
	public String toString() {
		return getName();
	}
	
	/** Reads a group definition object from the supplied stream. */
	public void readExternal(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		/*
		if(!ExternalizableHelper.isEOF(dis)){
			setGroupNo(dis.readByte());
			setName(dis.readUTF());
			setQuestions(ExternalizableHelper.readExternal(dis,new QuestionDef().getClass()));
		}
		*/
	}

	/** Write the group definition object to the supplied stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
/*
		dos.writeByte(getGroupNo());
		dos.writeUTF(getName());
		ExternalizableHelper.writeExternal(getQuestions(), dos);
*/
	}	
}
