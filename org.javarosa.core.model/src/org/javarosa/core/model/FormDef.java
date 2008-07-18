package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

/**
 * Definition of a form. This has some meta data about the form definition and  
 * a collection of groups together with question branching or skipping rules.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormDef implements IFormElement, Localizable, IDRecordable, Externalizable{
	private Vector children;	/** A collection of group definitions. */
	private Vector dataBindings;
	private int id;		/** The numeric unique identifier of the form definition. */	
	private String name;	/** The display name of the form. */
	private Localizer localizer;

	private IFormDataModel model;

	private int recordId; //does this belong here?
	
	// dunno about this...
	///** The collection of rules for this form. */
	//private Vector rules;
	
	// what is this?
	// /** A string constistig for form fields that describe its data. */
	//private String descriptionTemplate =  Constants.EMPTY_STRING;

	public FormDef() {
		setChildren(null);
	}
	
//	/**
//	 * Constructs a form definition object from these parameters.
//	 * 
//	 * @param name - the numeric unique identifier of the form definition.
//	 * @param name - the display name of the form.
//	 * @param variableName - the string unique identifier of the form definition.
//	 * @param groups - collection of group definitions.
//	 * @param rules - collection of branching rules.
//	 */
//	public FormDef(int id, String name, String variableName,Vector groups, Vector rules, String descTemplate) {
//		this();
//		setId(id);
//		setName(name);
//		setVariableName(variableName);
//		setGroups(groups);
//		setRules(rules);
//		setDescriptionTemplate((descTemplate == null) ? Constants.EMPTY_STRING : descTemplate);
//	}

	public Vector getChildren() {
		return children;
	}

	public void setChildren(Vector children) {
		this.children = (children == null ? new Vector() : children);
	}
	
	public void addChild (IFormElement fe) {
		children.addElement(fe);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public Localizer getLocalizer () {
		return localizer;
	}
	
	public void setLocalizer (Localizer l) {
		this.localizer = l;
	}
	
	public IFormDataModel getDataModel () {
		return model;
	}
	
	public void setDataModel (IFormDataModel model) {
		this.model = model;
	}
	
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	/*
	public Vector getRules() {
		return rules;
	}

	public void setRules(Vector rules) {
		this.rules = rules;
	}
	*/
	
	public Vector getBindings () {
		return dataBindings;
	}
	
	public void setBindings (Vector v) {
		this.dataBindings = v;
	}
	
	public void addBinding (DataBinding db) {
		if (dataBindings == null)
			dataBindings = new Vector();
		dataBindings.addElement(db);
	}
	
	/*
	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(String descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}
	*/
	
	public void localeChanged (String locale, Localizer localizer) {
		
	}
	
	public String toString() {
		return getName();
	}
	
//	/**
//	 * Gets a question identified by a variable name.
//	 * 
//	 * @param varName - the string identifier of the question. 
//	 * @return the question reference.
//	 */
//	public QuestionDef getQuestion(String varName){
//		if(varName == null)
//			return null;
//		
//		for(byte i=0; i<getGroups().size(); i++){
//			QuestionDef def = ((GroupDef)getGroups().elementAt(i)).getQuestion(varName);
//			if(def != null)
//				return def;
//		}
//		
//		return null;
//	}
//	
//	/**
//	 * Gets a numeric question identifier for a given question variable name.
//	 * 
//	 * @param varName - the string identifier of the question. 
//	 * @return the numeric question identifier.
//	 */
//	public String getQuestionId(String varName){
//		QuestionDef qtn = getQuestion(varName);
//		if(qtn != null)
//			return qtn.getId();
//		
//		return Constants.NULL_STRING_ID;
//	}
//
//	public void addQuestion(QuestionDef qtn){
//		if(groups == null){
//			groups = new Vector();
//			GroupDef group = new GroupDef(this.getVariableName(),Byte.parseByte("1"),null);
//			groups.addElement(group);
//		}
//		
//		((GroupDef)groups.elementAt(0)).addQuestion(qtn);
//	}
	
	/** 
	 * Reads the form definition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void readExternal(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		if(!ExternalizableHelper.isEOF(dis)){
			PrototypeFactory factory = new PrototypeFactory();
			factory.addNewPrototype(QuestionDef.class.getName(), QuestionDef.class);
			factory.addNewPrototype(GroupDef.class.getName(), GroupDef.class);
			setID(dis.readInt());
			setName(dis.readUTF());
			
			setChildren(ExternalizableHelper.readExternal(dis,factory));
			setBindings(ExternalizableHelper.readExternal(dis,new DataBinding().getClass()));
		}
	}

	/** 
	 * Writes the form definition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		dos.writeInt(getID());
		dos.writeUTF(getName());
		
		ExternalizableHelper.writeExternalGeneric(getChildren(), dos);
		ExternalizableHelper.writeExternal(getBindings(), dos);
	}
}
