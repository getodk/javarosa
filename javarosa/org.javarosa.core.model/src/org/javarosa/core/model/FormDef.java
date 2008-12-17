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
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.model.utils.QuestionPreloader;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Definition of a form. This has some meta data about the form definition and  
 * a collection of groups together with question branching or skipping rules.
 * 
 * @author Daniel Kayiwa, Drew Roos
 *
 */
public class FormDef implements IFormElement, Localizable, IDRecordable, Externalizable{
	private Vector children;	/** A collection of group definitions. */
	private int id;		/** The numeric unique identifier of the form definition. */	
	private String name;	/** The display name of the form. */
	private Localizer localizer;
	private Vector conditions; //<Condition>
	private DataModelTree model;

	private Hashtable conditionTriggerIndex; // <TreeReference, Vector<Condition>>
	private Hashtable conditionRepeatTargetIndex; //<TreeReference, Condition>; associates repeatable nodes with the Condition that determines their relevancy
	private EvaluationContext conditionEvalContext;
	
	private QuestionPreloader preloader = new QuestionPreloader();

	public FormDef() {
		setChildren(null);
		conditions = new Vector();
		conditionTriggerIndex = new Hashtable();
		conditionRepeatTargetIndex = new Hashtable();
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
		if (i < children.size()) {
			return (IFormElement)children.elementAt(i);
		} else {
			return null;
		}
	}
	
	public IFormElement getChild (FormIndex index) {
		IFormElement element = this;
		while (index != null) {
			element =  element.getChild(index.getLocalIndex());
			index = index.getNextLevel();
		}
		return element;
	}
	
	/**
	 * Dereference the form index and return a Vector of all interstitial nodes (top-level parent first; index target last)
	 * 
	 * Ignore 'new-repeat' node for now; just return/stop at ref to yet-to-be-created repeat node (similar to repeats that already exist)
	 * 
	 * @param index
	 * @return
	 */
	public Vector explodeIndex (FormIndex index) {
		Vector indexes = new Vector();
		Vector multiplicities = new Vector();
		Vector elements = new Vector();
		
		collapseIndex(index, indexes, multiplicities, elements);		
		return elements;
	}
	
	//take a reference, find the instance node it refers to (factoring in multiplicities)
	public TreeReference getChildInstanceRef (FormIndex index) {
		Vector indexes = new Vector();
		Vector multiplicities = new Vector();
		Vector elements = new Vector();
		
		collapseIndex(index, indexes, multiplicities, elements);		
		return getChildInstanceRef(elements, multiplicities);
	}

	public TreeReference getChildInstanceRef (Vector elements, Vector multiplicities) {
		if (elements.size() == 0)
			return null;
				
		//get reference for target element
		TreeReference ref = DataModelTree.unpackReference(((IFormElement)elements.lastElement()).getBind()).clone();
		for (int i = 0; i < ref.size(); i++) {
			ref.multiplicity.setElementAt(new Integer(0), i);
		}
		
		//fill in multiplicities for repeats along the way
		for (int i = 0; i < elements.size(); i++) {
			IFormElement temp = (IFormElement)elements.elementAt(i);
			if (temp instanceof GroupDef && ((GroupDef)temp).getRepeat()) {
				TreeReference repRef = DataModelTree.unpackReference(temp.getBind());
				if (repRef.isParentOf(ref, false)) {
					int repMult = ((Integer)multiplicities.elementAt(i)).intValue();
					ref.multiplicity.setElementAt(new Integer(repMult), repRef.size() - 1);
				} else {
					return null; //question/repeat hierarchy is not consistent with instance model and bindings
				}
			}
		}
		
		return ref;
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
	
	public DataModelTree getDataModel () {
		return model;
	}
	
	public void setDataModel (IFormDataModel model) {
		this.model = (DataModelTree)model;
	}
	
	//don't think this should ever be called
	public IDataReference getBind () {
		return null;
	}
	
	public void setValue (IAnswerData data, TreeReference ref) {
		setValue(data, ref, model.resolveReference(ref));
	}
	
	//do we ever need to run this w/o evaluating conditions? or will we always just call on node directly?
	public void setValue (IAnswerData data, TreeReference ref, TreeElement node) {
		node.setAnswer(data);
		evaluateConditions(ref);		
	}
		
	public void createNewRepeat (FormIndex index) {
		TreeReference destRef = getChildInstanceRef(index);
		TreeElement template = model.getTemplate(destRef);
		
		model.copyNode(template, destRef);

		preloadModel(model.resolveReference(destRef));
		evaluateConditions(destRef); //trigger conditions that depend on the creation of this new node
		initializeConditions(destRef); //initialize conditions for the node (and sub-nodes)
	}
	
	public boolean canCreateRepeat (TreeReference repeatRef) {
		Condition c = (Condition)conditionRepeatTargetIndex.get(repeatRef.genericize());
		if (c != null) {
			return c.eval(model, new EvaluationContext(conditionEvalContext, repeatRef));
		} /* else
		  check # child constraints of parent
		} */
		
		return true;
	}
	
	/**
	 * Add a Condition to the form's Collection.
	 * @param condition the condition to be set
	 */
	public Condition addCondition (Condition condition) {
		int p = conditions.indexOf(condition);
		if (p >= 0) {
			//one node may control access to many nodes; this means many nodes effectively have the same condition
			//let's identify when conditions are the same, and store and calculate it only once

			//note, if the contextRef is unnecessarily deep, the condition will be evaluated more times than needed
			//perhaps detect when 'identical' condition has a shorter contextRef, and use that one instead?
			return (Condition)conditions.elementAt(p);
		} else {
			conditions.addElement(condition);
			
			Vector triggers = condition.getTriggers();
			for (int i = 0; i < triggers.size(); i++) {
				TreeReference trigger = (TreeReference)triggers.elementAt(i);
				if (!conditionTriggerIndex.containsKey(trigger)) {
					conditionTriggerIndex.put(trigger, new Vector());
				}
				Vector triggeredConditions = (Vector)conditionTriggerIndex.get(trigger);
				if (!triggeredConditions.contains(condition)) {
					triggeredConditions.addElement(condition);
				}
			}
			
			Vector targets = condition.getTargets();
			for (int i = 0; i < targets.size(); i++) {
				TreeReference target = (TreeReference)targets.elementAt(i);
				if (model.getTemplate(target) != null) {
					conditionRepeatTargetIndex.put(target, condition);
				}
			}
			
			return condition;
		}
	}

	public void initializeConditions () {
		initializeConditions(TreeReference.rootRef());
	}
	
	/**
	 * Walks the current set of conditions, and evaluates each of them with the 
	 * current context.
	 */
	public void initializeConditions (TreeReference rootRef) {
		TreeReference genericRoot = rootRef.genericize();
		
		for (int i = 0; i < conditions.size(); i++) {
			Condition c = (Condition)conditions.elementAt(i);
			boolean applicable = false;
			for (int j = 0; j < c.getTargets().size(); j++) {
				TreeReference target = (TreeReference)c.getTargets().elementAt(j);
				if (genericRoot.isParentOf(target, false)) {
					applicable = true;
					break;
				}
			}
			
			if (applicable) {
				evaluateCondition(c, rootRef);
			}
		}
	}

	//ref: unambiguous ref of node that just changed
	public void evaluateConditions (TreeReference ref) {
		//turn unambiguous ref into a generic ref
		TreeReference genericRef = ref.genericize();
		
		//get conditions triggered by this node
		Vector conditions = (Vector)conditionTriggerIndex.get(genericRef);
		if (conditions == null)
			return;

		//for each condition
		for (int i = 0; i < conditions.size(); i++) {
			Condition condition = (Condition)conditions.elementAt(i);
			evaluateCondition(condition, ref);
		}
	}
	
	private void evaluateCondition (Condition c, TreeReference anchorRef) {
		TreeReference contextRef = c.contextRef.contextualize(anchorRef);
		Vector v = model.expandReference(contextRef);
		for (int j = 0; j < v.size(); j++) {
			EvaluationContext ec = new EvaluationContext(conditionEvalContext, (TreeReference)v.elementAt(j));
			c.apply(model, ec);
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
//	public QuestionDef getQuesitonByID (int questionID) {
//		for (int i = 0; i < children.size(); i++) {
//			QuestionDef q = (QuestionDef)children.elementAt(i);
//			if (questionID == q.getID()) {
//				return q;
//			}
//		}
//		
//		return null;
//	}
				
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
	 * Preload the Data Model with the preload values that are enumerated in
	 * the data bindings.
	 */
	public void preloadModel(TreeElement node) {
		if (node.isLeaf()) {
			IAnswerData preload = null;
			if (node.preloadHandler != null) {
				preload = preloader.getQuestionPreload(node.preloadHandler, node.preloadParams);
			}
			if (preload != null) { //what if we want to wipe out a value in the instance?
				node.setAnswer(preload);
			}			
		} else {
			for (int i = 0; i < node.getNumChildren(); i++) {
				TreeElement child = (TreeElement)node.getChildren().elementAt(i);
				if (child.getMult() != TreeReference.INDEX_TEMPLATE) //don't preload templates; new repeats are preloaded as they're created
					preloadModel(child);
			}
		}
	}
	
	public boolean postProcessModel () {
		return postProcessModel(model.getRoot());
	}
	
	/**
	 * Iterate over the form's data bindings, and evaluate all post procesing calls.
	 * 
	 * @return true if the model was modified in any way. false otherwise.
	 */
	private boolean postProcessModel (TreeElement node) {
		//we might have issues with ordering, for example, a handler that writes a value to a node,
		//and a handler that does something external with the node. if both handlers are bound to the
		//same node, we need to make sure the one that alters the node executes first. deal with that later.
		//can we even bind multiple handlers to the same node currently?
		
		//also have issues with conditions. it is hard to detect what conditions are affected by the actions
		//of the post-processor. normally, it wouldn't matter because we only post-process when we are exiting
		//the form, so the result of any triggered conditions is irrelevant. however, if we save a form in the
		//interim, post-processing occurs, and then we continue to edit the form. it seems like having conditions
		//dependent on data written during post-processing is a bad practice anyway, and maybe we shouldn't support it.
		
		if (node.isLeaf()) {
			if (node.preloadHandler != null) {
				return preloader.questionPostProcess(node, node.preloadHandler, node.preloadParams);
			} else {
				return false;
			}
		} else {
			boolean modelModified = false;			
			for (int i = 0; i < node.getNumChildren(); i++) {
				TreeElement child = (TreeElement)node.getChildren().elementAt(i);
				if (child.getMult() != TreeReference.INDEX_TEMPLATE)
					modelModified |= postProcessModel(child);
			}
			return modelModified;
		}
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
		setID(ExtUtil.readInt(dis));
		setName((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setChildren((Vector)ExtUtil.read(dis, new ExtWrapListPoly(), pf));
		
		model = (DataModelTree)ExtUtil.read(dis, DataModelTree.class, pf);
		model.setFormId(getID());

		setLocalizer((Localizer)ExtUtil.read(dis, new ExtWrapNullable(Localizer.class), pf));
		
		Vector vcond = (Vector)ExtUtil.read(dis, new ExtWrapList(Condition.class), pf);
		for (Enumeration e = vcond.elements(); e.hasMoreElements(); )
			addCondition((Condition)e.nextElement());		
	}

	/**
	 * meant to be called after deserialization and initialization of handlers
	 * 
	 * @param newInstance true if the form is to be used for a new entry interaction,
	 * false if it is using an existing IDataModel
	 */
	public void initialize (boolean newInstance) {
		if (newInstance) {//only preload new forms (we may have to revisit this)
			preloadModel(model.getRoot());
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
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapNullable(getName()));
		ExtUtil.write(dos, new ExtWrapListPoly(getChildren()));
		ExtUtil.write(dos, model);
		ExtUtil.write(dos, new ExtWrapNullable(localizer));
		ExtUtil.write(dos, new ExtWrapList(conditions));
	}

	public void collapseIndex (FormIndex index, Vector indexes, Vector multiplicities, Vector elements) {
		if (!index.isInForm()) {
			return;
		}
		
		IFormElement element = this;
		while (index != null) {
			int i = index.getLocalIndex();			
			element = element.getChild(i);
			if (element == null)
				throw new ArrayIndexOutOfBoundsException("invalid index");
			
			indexes.addElement(new Integer(i));
			multiplicities.addElement(new Integer(index.getInstanceIndex() == -1 ? 0 : index.getInstanceIndex()));
			elements.addElement(element);
			
			index = index.getNextLevel();
		}
	}
	
	public FormIndex buildIndex (Vector indexes, Vector multiplicities, Vector elements) {
		FormIndex cur = null;
		for (int i = indexes.size() - 1; i >= 0; i--) {
			int ix = ((Integer)indexes.elementAt(i)).intValue();
			int mult = ((Integer)multiplicities.elementAt(i)).intValue();
			if (!(elements.elementAt(i) instanceof GroupDef && ((GroupDef)elements.elementAt(i)).getRepeat())) {
				mult = -1;
			}

			cur = new FormIndex(cur, ix, mult);
		}
		return cur;
	}
	
	public FormIndex incrementIndex (FormIndex index) {
		Vector indexes = new Vector();
		Vector multiplicities = new Vector();
		Vector elements = new Vector();
		
		if (index.isEndOfFormIndex()) {
			return index;
		} else if (index.isBeginningOfFormIndex()) {
			if (children == null || children.size() == 0) {
				return FormIndex.createEndOfFormIndex();
			}
		} else {
			collapseIndex(index, indexes, multiplicities, elements);
		}
			
		incrementHelper(indexes, multiplicities, elements);
	
		if (indexes.size() == 0) {
			return FormIndex.createEndOfFormIndex();
		} else {
			return buildIndex(indexes, multiplicities, elements);
		}
	}
	
	private void incrementHelper (Vector indexes, Vector multiplicities, Vector elements) {
		int i = indexes.size() - 1;
		boolean exitRepeat = false;
		
		if (i == -1 || elements.elementAt(i) instanceof GroupDef) {
			//current index is group or repeat or the top-level form
			boolean descend = true;
			
			if (i >= 0) {
				//find out whether we're on a repeat, and if so, whether the specified instance actually exists
				GroupDef group = (GroupDef)elements.elementAt(i);
				if (group.getRepeat()) {
					if (model.resolveReference(getChildInstanceRef(elements, multiplicities)) == null) {
						descend = false; //repeat instance does not exist; do not descend into it
						exitRepeat = true;
					}
				}
			}
			
			if (descend) {
				indexes.addElement(new Integer(0));
				multiplicities.addElement(new Integer(0));
				elements.addElement((i == -1 ? this : (IFormElement)elements.elementAt(i)).getChild(0));
				return;
			}
		}
		
		while (i >= 0) {
			//if on repeat, increment to next repeat EXCEPT when we're on a repeat instance that does not exist and was not created
			//  (repeat-not-existing can only happen at lowest level; exitRepeat will be true)
			if (!exitRepeat && elements.elementAt(i) instanceof GroupDef && ((GroupDef)elements.elementAt(i)).getRepeat()) {
				multiplicities.setElementAt(new Integer(((Integer)multiplicities.elementAt(i)).intValue() + 1), i);
				return;
			}
			
			IFormElement parent = (i == 0 ? this : (IFormElement)elements.elementAt(i - 1));
		    int curIndex = ((Integer)indexes.elementAt(i)).intValue();

		    //increment to the next element on the current level
		    if (curIndex + 1 >= parent.getChildren().size()) {
		    	//at the end of the current level; move up one level and start over
		    	indexes.removeElementAt(i);
		    	multiplicities.removeElementAt(i);
		    	elements.removeElementAt(i);
		    	i--;
		    	exitRepeat = false;
		    } else {
		    	indexes.setElementAt(new Integer(curIndex + 1), i);
		    	multiplicities.setElementAt(new Integer(0), i);
		    	elements.setElementAt(parent.getChild(curIndex + 1), i);
		    	return;
		    }
		}
	}	

	public FormIndex decrementIndex (FormIndex index) {
		Vector indexes = new Vector();
		Vector multiplicities = new Vector();
		Vector elements = new Vector();
		
		if (index.isBeginningOfFormIndex()) {
			return index;
		} else if (index.isEndOfFormIndex()) {
			if (children == null || children.size() == 0) {
				return FormIndex.createBeginningOfFormIndex();
			}
		} else {
			collapseIndex(index, indexes, multiplicities, elements);
		}
			
		decrementHelper(indexes, multiplicities, elements);
	
		if (indexes.size() == 0) {
			return FormIndex.createBeginningOfFormIndex();
		} else {
			return buildIndex(indexes, multiplicities, elements);
		}
	}
	
	private void decrementHelper (Vector indexes, Vector multiplicities, Vector elements) {		
		int i = indexes.size() - 1;
		
		if (i != -1) {
			int curIndex = ((Integer)indexes.elementAt(i)).intValue();
			int curMult = ((Integer)multiplicities.elementAt(i)).intValue();

			if (curMult > 0) {
				//set node to previous repetition of current element
				multiplicities.setElementAt(new Integer(curMult - 1), i);
			} else if (curIndex > 0) {	
				//set node to previous element
		    	indexes.setElementAt(new Integer(curIndex - 1), i);
		    	multiplicities.setElementAt(new Integer(0), i);
		    	elements.setElementAt((i == 0 ? this : (IFormElement)elements.elementAt(i - 1)).getChild(curIndex - 1), i);
				
		    	if (setRepeatNextMultiplicity(elements, multiplicities))
		    		return;
			} else {
				//at absolute beginning of current level; index to parent
		    	indexes.removeElementAt(i);
		    	multiplicities.removeElementAt(i);
		    	elements.removeElementAt(i);
		    	return;
			}
		}
		
		IFormElement element = (i < 0 ? this : (IFormElement)elements.elementAt(i));
		while (!(element instanceof QuestionDef)) {
			int subIndex = element.getChildren().size() - 1;
			element = element.getChild(subIndex);
			
	    	indexes.addElement(new Integer(subIndex));
	    	multiplicities.addElement(new Integer(0));
	    	elements.addElement(element);

	    	if (setRepeatNextMultiplicity(elements, multiplicities))
	    		return;
		}		
	}
	
	private boolean setRepeatNextMultiplicity (Vector elements, Vector multiplicities) {
    	//find out if node is repeatable
    	TreeReference nodeRef = getChildInstanceRef(elements, multiplicities);
    	TreeElement node = model.resolveReference(nodeRef);
    	if (node == null || node.repeatable) { //node == null if there are no instances of the repeat
    		int mult;
    		if (node == null) {
    			mult = 0; //no repeats; next is 0
    		} else {
	    		String name = node.getName();
	    		TreeElement parentNode = model.resolveReference(nodeRef.getParentRef());
	    		mult = parentNode.getChildMultiplicity(name);
    		}
    		multiplicities.setElementAt(new Integer(mult), multiplicities.size() - 1);
    		return true;
    	} else {
    		return false;
    	}
	}

	/*
	 * (non-Javadoc)
	 * 
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
	
	public void registerStateObserver (FormElementStateListener qsl) {
		//NO. (Or at least not yet).
	}
	
	public void unregisterStateObserver (FormElementStateListener qsl) {
		//NO. (Or at least not yet).
	}
}
