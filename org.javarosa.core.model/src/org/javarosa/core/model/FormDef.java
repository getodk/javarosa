package org.javarosa.core.model;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.model.utils.QuestionPreloader;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;

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
	private Vector conditions;
	private IFormDataModel model;

	private Hashtable conditionTriggerIndex; /* String IDataReference -> Vector of Condition */
	private EvaluationContext conditionEvalContext;
	
	private QuestionPreloader preloader = new QuestionPreloader();
	private PrototypeFactoryDeprecated modelFactory;

	public FormDef() {
		setChildren(null);
		conditions = new Vector();
		conditionTriggerIndex = new Hashtable();
	}
	
	public Vector getChildren() {
		return children;
	}

	public void setChildren(Vector children) {
		this.children = (children == null ? new Vector() : children);
	}
	
	public void addChild (IFormElement fe) {
		children.addElement(fe);
	}
	
	public IFormElement getChild (int i) {
		return (IFormElement)children.elementAt(i);
	}
	
	//TODO: need functions that provide means of walking the tree and intuitively accessing sub-children
	
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
	}
	
	public void setRecordId(int recordId) {
		setID(recordId);
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
	
	/**
	 * @param question The question whose data should be retrieved
	 * @return The value, if any, associated with this question in the
	 * form's underlying data model.
	 */
	public IAnswerData getValue (QuestionDef question) {
		return model.getDataValue(question.getBind());
	}
	
	/**
	 * Sets the value associated with the given question in the form's
	 * underlying data model.
	 * 
	 * @param question the question whose data should be set
	 * @param data The new data value
	 */
	public void setQuestionValue (QuestionDef question, IAnswerData data) {
		if (setValue(question.getBind(), data)) {
			question.alertStateObservers(QuestionStateListener.CHANGE_DATA);			
		}
	}
	
	/**
	 * Sets the value associated with the given data reference in the
	 * form's underlying data model
	 * 
	 * @param ref The reference to the value which should be set
	 * @param data the new data value.
	 * @return Whether or not a value was updated in the model.
	 */
	public boolean setValue (IDataReference ref, IAnswerData data) {
		boolean updated = model.updateDataValue(ref, data);
		if (updated) {
			evaluateConditions(ref);
		}
		return updated;
	}
	
	/**
	 * Add a Condition to the form's Collection.
	 * @param condition the condition to be set
	 */
	public Condition addCondition (Condition condition) {
		for (int i = 0; i < conditions.size(); i++) {
			Condition c = (Condition)conditions.elementAt(i);
			if (c.equals(condition))
				return c;
		}
		conditions.addElement(condition);
		condition.attachForm(this);
		
		Vector triggers = condition.getTriggers();
		for (int i = 0; i < triggers.size(); i++) {
			IDataReference trigger = (IDataReference)triggers.elementAt(i);
			if (!conditionTriggerIndex.containsKey(trigger)) {
				conditionTriggerIndex.put(trigger, new Vector());
			}
			Vector triggeredConditions = (Vector)conditionTriggerIndex.get(trigger);
			if (!triggeredConditions.contains(condition)) {
				triggeredConditions.addElement(condition);
			}
		}
		
		return condition;
	}

	/**
	 * Walks the current set of conditions, and evaluates each of them with the 
	 * current context.
	 */
	public void initializeConditions () {
		for (int i = 0; i < conditions.size(); i++) {
			((Condition)conditions.elementAt(i)).eval(model, conditionEvalContext);
		}
	}
	
	/**
	 * Evaluates any conditions that are associated with the value determined
	 * by the given reference.
	 * 
	 * @param ref The reference. NOTE: Currently this reference's getReference()
	 * value must be a String datatype. This should be dealt with in a more
	 * general way in the future.
	 */
	public void evaluateConditions (IDataReference ref) {
		Vector conditions = (Vector)conditionTriggerIndex.get(ref);
		if (conditions == null)
			return;
		
		for (int i = 0; i < conditions.size(); i++) {
			Condition condition = (Condition)conditions.elementAt(i);
			condition.eval(model, conditionEvalContext);
		}
	}
	
	/**
	 * @param ec The new Evaluation Context
	 */
	public void setEvaluationContext (EvaluationContext ec) {
		this.conditionEvalContext = ec;
	}
	
	/**
	 * Note that this method doesn't yet deal with groups in any
	 * way, and will fail if this form contains any groups.
	 * 
	 * @param questionID the unique integer ID of the question.
	 * @return A quesiton associated with the given ID. 
	 */
	public QuestionDef getQuesitonByID (int questionID) {
		for (int i = 0; i < children.size(); i++) {
			QuestionDef q = (QuestionDef)children.elementAt(i);
			if (questionID == q.getID()) {
				return q;
			}
		}
		
		return null;
	}
	
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

	/**
	 * @param db A new DataBinding to be added to the form's list.
	 */
	public void addBinding (DataBinding db) {
		if (dataBindings == null)
			dataBindings = new Vector();
		dataBindings.addElement(db);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.Localizable#localeChanged(java.lang.String, org.javarosa.core.model.utils.Localizer)
	 */
	public void localeChanged (String locale, Localizer localizer) {
		for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
			((IFormElement)e.nextElement()).localeChanged(locale, localizer);
		}
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * @param modelFactory the PrototypeFactoryDeprecated that should be used to deserialize IDataModel
	 * objects.
	 */
	public void setModelFactory(PrototypeFactoryDeprecated modelFactory) {
		this.modelFactory = modelFactory;
	}
	
	/**
	 * Preload the Data Model with the preload values that are enumerated in
	 * the data bindings.
	 */
	public void preloadModel() {
		IAnswerData preload = null;
		
		// 25/08/2008 BWD 
		// Fixed bug where it expected bindings.  Not all forms have bindings!
		if(getBindings() == null)
			return;
		
		Enumeration en = getBindings().elements();
		while(en.hasMoreElements()) {
			DataBinding binding = (DataBinding)en.nextElement();
			preload = null;
			if (binding.getPreload() != null)
				preload = preloader.getQuestionPreload(binding.getPreload(), binding.getPreloadParams());
			if(preload != null) { //what if we want to wipe out a value in the instance?
				setValue(binding.getReference(), preload);
			}
		}
	}
	
	/**
	 * Iterate over the form's data bindings, and evaluate all post procesing calls.
	 * 
	 * @return true if the model was modified in any way. false otherwise.
	 */
	public boolean postProcessModel () {
		boolean modelModified = false;
		
		Enumeration en = getBindings().elements();
		//we might have issues with ordering, for example, a handler that writes a value to a node,
		//and a handler that does something external with the node. if both handlers are bound to the
		//same node, we need to make sure the one that alters the node executes first. deal with that later.
		
		//also have issues with conditions. it is hard to detect what conditions are affected by the actions
		//of the post-processor. normally, it wouldn't matter because we only post-process when we are exiting
		//the form, so the result of any triggered conditions is irrelevant. however, if we save a form in the
		//interim, post-processing occurs, and then we continue to edit the form. it seems like having conditions
		//dependent on data written during post-processing is a bad practice anyway, and maybe we shouldn't support it.
		while(en.hasMoreElements()) {
			DataBinding binding = (DataBinding)en.nextElement();
			if (binding.getPreload() != null) {
				modelModified = preloader.questionPostProcess(binding.getReference(), binding.getPreload(), binding.getPreloadParams(), model)
					|| modelModified;
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
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		if(!ExternalizableHelperDeprecated.isEOF(dis)){
			PrototypeFactoryDeprecated factory = new PrototypeFactoryDeprecated();
			factory.addNewPrototype(QuestionDef.class.getName(), QuestionDef.class);
			factory.addNewPrototype(GroupDef.class.getName(), GroupDef.class);
			setID(dis.readInt());
			setName(ExternalizableHelperDeprecated.readUTF(dis));
			
			setChildren(ExternalizableHelperDeprecated.readExternal(dis,factory));
			setBindings(ExternalizableHelperDeprecated.readExternal(dis,new DataBinding().getClass()));
			
			String modelType = dis.readUTF();
			model = (IFormDataModel)modelFactory.getNewInstance(modelType);
			if(model  == null) { 
				throw new DeserializationException("FormDef was unable to deserialize the Model Template, " +
						"due to a missing prototype. Please set the model to a prototype before deserialization.");
			}
			model.setFormReferenceId(this.getID());
			model.readExternal(dis, pf);

			setLocalizer((Localizer)ExternalizableHelperDeprecated.readExternalizable(dis, new Localizer()));
			
			Vector conditionList = ExternalizableHelperDeprecated.readExternal(dis, Condition.class);
			if (conditionList != null) {
				for (int i = 0; i < conditionList.size(); i++)
					this.addCondition((Condition)conditionList.elementAt(i));
			}
		}
	}

	/**
	 * meant to be called after deserialization and initialization of handlers
	 * 
	 * @param newInstance true if the form is to be used for a new entry interaction,
	 * false if it is using an existing IDataModel
	 */
	public void initialize (boolean newInstance) {
		if (newInstance) {//only preload new forms (we may have to revisit this)
			preloadModel();
		}
		
		initializeConditions();
		
		if (getLocalizer() != null && getLocalizer().getLocale() == null) {
			getLocalizer().setToDefault();
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
		ExternalizableHelperDeprecated.writeUTF(dos, getName());
		
		ExternalizableHelperDeprecated.writeExternalGeneric(getChildren(), dos);
		ExternalizableHelperDeprecated.writeExternal(getBindings(), dos);

		dos.writeUTF(model.getClass().getName());
		model.writeExternal(dos);
		
		ExternalizableHelperDeprecated.writeExternalizable(localizer, dos);
		
		ExternalizableHelperDeprecated.writeExternal(conditions, dos);
	}
}
