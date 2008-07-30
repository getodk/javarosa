package org.javarosa.core.model;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.model.utils.QuestionPreloader;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * Definition of a form. This has some meta data about the form definition and  
 * a collection of groups together with question branching or skipping rules.
 * 
 * @author Daniel Kayiwa
 *
 */
public class FormDef implements IFormElement, Localizable, IDRecordable, Externalizable{
	private Vector children;	/** A collection of group definitions. */
	private Vector dataBindings = new Vector();
	private int id;		/** The numeric unique identifier of the form definition. */	
	private String name;	/** The display name of the form. */
	private Localizer localizer;
	
	private QuestionPreloader preloader = new QuestionPreloader();
	
	private PrototypeFactory modelFactory;

	private IFormDataModel model;

	//private int recordId;
	
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
		//if (localizer != null && localizer.getLocale() != null)
		//	fe.localeChanged(localizer.getLocale(), localizer);
	}
	
	public IFormElement getChild (int i) {
		return (IFormElement)children.elementAt(i);
	}
	
	//need functions that provide means of walking the tree and intuitively accessing sub-children
	
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
	
	//treating id and record id as the same until we resolve the need for both of them
	public int getRecordId () {
		return getID();
		//return recordId;
	}
	
	public void setRecordId(int recordId) {
		setID(recordId);
		//this.recordId = recordId;
	}
	
	public Localizer getLocalizer () {
		return localizer;
	}
	
	public void setLocalizer (Localizer l) {
		if (this.localizer != null) {
			this.localizer.unregisterLocalizable(this);
		}
		
		this.localizer = l;
		if (this.localizer != null) {
			this.localizer.registerLocalizable(this);
		}
	}
	
	public IFormDataModel getDataModel () {
		return model;
	}
	
	public void setDataModel (IFormDataModel model) {
		this.model = model;
	}
	
	public IAnswerData getValue (QuestionDef question) {
		return model.getDataValue(question.getBind());
	}
	
	public void setValue (QuestionDef question, IAnswerData data) {
		boolean updated = model.updateDataValue(question.getBind(), data);
		if (updated) {
			question.alertStateObservers(QuestionStateListener.CHANGE_DATA);
		}
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
	
	/**
	 * @return the preloads
	 */
	public QuestionPreloader getPreloader() {
		return preloader;
	}

	/**
	 * @param preloads the preloads to set
	 */
	public void setPreloader(QuestionPreloader preloads) {
		this.preloader = preloads;
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
		for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
			((IFormElement)e.nextElement()).localeChanged(locale, localizer);
		}
	}
	
	public String toString() {
		return getName();
	}
	
	public void setModelFactory(PrototypeFactory modelFactory) {
		this.modelFactory = modelFactory;
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
	 * Preload the Data Model with the preload values that are enumerated in
	 * the data bindings.
	 */
	public void preloadModel() {
		Enumeration en = getBindings().elements();
		while(en.hasMoreElements()) {
			DataBinding binding = (DataBinding)en.nextElement();
			IAnswerData preload = preloader.getQuestionPreload(binding.getPreload(), binding.getPreloadParams());
			if(preload != null) {
				model.updateDataValue(binding.getReference(), preload);
			}
		}
	}
	
	//TODO: make this all generic like preloadModel
	public boolean postProcessModel () {
		boolean modelModified = false;
		
		for (Enumeration e = dataBindings.elements(); e.hasMoreElements(); ) {
			DataBinding b = (DataBinding)e.nextElement();
			IDataReference ref = b.getReference();

			//model-modifying operations should come first
			if ("timestamp".equals(b.getPreload()) && "end".equals(b.getPreloadParams())) {
				model.updateDataValue(ref, new StringData(DateUtils.formatDateToTimeStamp(new Date())));
				modelModified = true;
			} else if ("property".equals(b.getPreload())) {
				String propName = b.getPreloadParams();
				IAnswerData answer = model.getDataValue(ref);
				String value = (answer == null ? null : answer.getDisplayText());
				if (propName != null && propName.length() > 0 && value != null && value.length() > 0)
					JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propName, value);
			}
		}
			
		return modelModified;
	}
	
	/** 
	 * Reads the form definition object from the supplied stream.
	 * 
	 * Requires that the model has been set to a prototype of the model that should
	 * be used for deserialization.
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
			setName(ExternalizableHelper.readUTF(dis));
			
			setChildren(ExternalizableHelper.readExternal(dis,factory));
			setBindings(ExternalizableHelper.readExternal(dis,new DataBinding().getClass()));
			
			String modelType = dis.readUTF();
			model = (IFormDataModel)modelFactory.getNewInstance(modelType);
			if(model  == null) { 
				throw new UnavailableExternalizerException("FormDef was unable to deserialize the Model Template, " +
						"due to a missing prototype. Please set the model to a prototype before deserialization.");
			}
			model.setFormReferenceId(this.getID());
			model.readExternal(dis);

			setLocalizer((Localizer)ExternalizableHelper.readExternalizable(dis, new Localizer()));
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
		ExternalizableHelper.writeUTF(dos, getName());
		
		ExternalizableHelper.writeExternalGeneric(getChildren(), dos);
		ExternalizableHelper.writeExternal(getBindings(), dos);

		dos.writeUTF(model.getClass().getName());
		model.writeExternal(dos);
		
		ExternalizableHelper.writeExternalizable(localizer, dos);
	}
}
