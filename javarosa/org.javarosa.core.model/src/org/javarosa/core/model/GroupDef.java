package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/** The definition of a group in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements IFormElement, Localizable {
	private Vector children;	/** A list of questions on a group. */	
	private boolean repeat;
	private int id;	/** The group number. */
	private String name;	/** The name of the group. */
	private IDataReference binding;	/** reference to a location in the model to store data in */
	
	private String longText;
	private String longTextID;
	private String shortText;
	private String shortTextID;
	
	Vector observers;
	
	public boolean noAddRemove = false;
	//public boolean startEmpty = false;
	public IDataReference count = null;
	
	public GroupDef () {
		this(Constants.NULL_ID, null, null, false);
	}
	
	public GroupDef(int id, String name, Vector children, boolean repeat) {
		setTitle(name);
		setID(id);
		setChildren(children);
		setRepeat(repeat);
		observers = new Vector();
	}
	
	public int getID () {
		return id;
	}
	
	public void setID (int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return name;
	}

	public void setTitle(String name) {
		this.name = name;
	}

	public IDataReference getBind() {
		return binding;
	}
	
	public void setBind(IDataReference binding) {
		this.binding = binding;
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
	
	public IFormElement getChild (int i) {
		if (children == null || i >= children.size()) {
			return null;
		} else {
			return (IFormElement)children.elementAt(i);
		}
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
    	
    	for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
    		((IFormElement)e.nextElement()).localeChanged(locale, localizer);
    	}
    }
	
	public String toString() {
		return getTitle();
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormElement#getDeepChildCount()
	 */
	public int getDeepChildCount() {
		int total = 0;
		Enumeration e = children.elements();
		while(e.hasMoreElements()) {
			total += ((IFormElement)e.nextElement()).getDeepChildCount();
		}
		return total;
	}

	/** Reads a group definition object from the supplied stream. */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		setID(ExtUtil.readInt(dis));
		setTitle((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setBind((IDataReference)ExtUtil.read(dis, new ExtWrapTagged(), pf));
		setLongText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setShortText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setLongTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);
		setShortTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);
		setRepeat(ExtUtil.readBool(dis));
		setChildren((Vector)ExtUtil.read(dis, new ExtWrapListPoly(), pf));
		
		//TODO: custom group parameters
	}

	/** Write the group definition object to the supplied stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapNullable(getTitle()));
		ExtUtil.write(dos, new ExtWrapTagged(getBind()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongText()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortText()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortTextID()));				
		ExtUtil.writeBool(dos, getRepeat());
		ExtUtil.write(dos, new ExtWrapListPoly(getChildren()));

		//TODO: custom group parameters
	}
	
	public void registerStateObserver (FormElementStateListener qsl) {
		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}
	
	public void unregisterStateObserver (FormElementStateListener qsl) {
		observers.removeElement(qsl);
	}
}
