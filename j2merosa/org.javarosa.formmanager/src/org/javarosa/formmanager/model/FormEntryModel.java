package org.javarosa.formmanager.model;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.formmanager.utility.FormEntryModelListener;

public class FormEntryModel {
    private FormDef form;

    private FormIndex activeQuestionIndex;
    private FormIndex startIndex;
    private int instanceID;
    private boolean unsavedChanges;
    private boolean formCompleted;
    
    private Vector observers;
    
    public int totalQuestions; //total number of questions in the form; used for progress bar

	private boolean readOnly;
    
    public FormEntryModel(FormDef form) {
    	this(form, -1);
    }
    
    public FormEntryModel(FormDef form, int instanceID) {
    	this(form, instanceID, null);
    }
    
    public FormEntryModel(FormDef form, int instanceID, FormIndex firstIndex) {
    	this.form = form;
    	this.instanceID = instanceID;
    	this.observers = new Vector();
    	
    	this.activeQuestionIndex = FormIndex.createBeginningOfFormIndex();
    	this.unsavedChanges = true; //we want them to be able to save the form initially, even with nothing in it
    	this.formCompleted = false;
    	this.startIndex = firstIndex;
    }
    
    public FormIndex getQuestionIndex () {
    	return activeQuestionIndex;
    }
    
    public void setQuestionIndex (FormIndex index) {
    	if (!activeQuestionIndex.equals(index)) {
    		activeQuestionIndex = index;
    		
    		for (Enumeration e = observers.elements(); e.hasMoreElements(); ) {
    			((FormEntryModelListener)e.nextElement()).questionIndexChanged(activeQuestionIndex);   			
    		}
    	}
    }
    
    public FormDef getForm () {
    	return form;
    }
    
    public int getInstanceID () {
    	return instanceID;
    }
    
    public boolean isSaved () {
    	return !unsavedChanges;
    }
    
    public void modelChanged () {
    	if (!unsavedChanges) {
    		unsavedChanges = true;
    		
    		for (Enumeration e = observers.elements(); e.hasMoreElements(); ) {
    			((FormEntryModelListener)e.nextElement()).saveStateChanged(instanceID, unsavedChanges);   			
    		}		
    	}
    }
    
    public void modelSaved (int instanceID) {
    	this.instanceID = instanceID;
    	unsavedChanges = false;
    	
		for (Enumeration e = observers.elements(); e.hasMoreElements(); ) {
			((FormEntryModelListener)e.nextElement()).saveStateChanged(instanceID, unsavedChanges);   			
		}	  	
    }
        
    public boolean isFormComplete () {
    	return formCompleted; 
    }
    
    public void setFormComplete () {
    	if (!formCompleted) {
    		formCompleted = true;

    		if(!activeQuestionIndex.isEndOfFormIndex()) {
    			setQuestionIndex(FormIndex.createEndOfFormIndex());
    		}
    		
    		for (Enumeration e = observers.elements(); e.hasMoreElements(); ) {
    			((FormEntryModelListener)e.nextElement()).formComplete();   			
    		}	 
    	}
    }
    
    public void notifyStartOfForm () {
		for (Enumeration e = observers.elements(); e.hasMoreElements(); ) {
			((FormEntryModelListener)e.nextElement()).startOfForm();   			
		}	     	
    }
    
    public int getNumQuestions () {
    	return form.getDeepChildCount();
    }
    
    protected boolean isAskNewRepeat (FormIndex questionIndex) {
    	Vector defs = form.explodeIndex(questionIndex);
    	IFormElement last = (defs.size() == 0 ? null : (IFormElement)defs.lastElement());
    	if (last instanceof GroupDef &&
    		((GroupDef)last).getRepeat() &&
    		form.getDataModel().resolveReference(form.getChildInstanceRef(questionIndex)) == null) {
    			return true;
    	}
    	return false;
    }
    
    public boolean isReadonly (FormIndex questionIndex) {
    	TreeReference ref = form.getChildInstanceRef(questionIndex);
    	boolean isAskNewRepeat = isAskNewRepeat(questionIndex);
    	    	
    	if (isAskNewRepeat) {
    		 return false;
    	} else {
        	TreeElement node = form.getDataModel().resolveReference(ref);
        	return !node.isEnabled();
    	}
    }
    
    public boolean isRelevant (FormIndex questionIndex) {
    	TreeReference ref = form.getChildInstanceRef(questionIndex);
    	boolean isAskNewRepeat = isAskNewRepeat(questionIndex);
    	    	
    	boolean relevant;
    	if (isAskNewRepeat) {
    		relevant = form.canCreateRepeat(ref);
    	} else {
        	TreeElement node = form.getDataModel().resolveReference(ref);
        	relevant = node.isRelevant();  //check instance flag first
    	}

    	if (relevant) { //if instance flag/condition says relevant, we still have to check the <group>/<repeat> hierarchy
        	Vector defs = form.explodeIndex(questionIndex);
    		
    		FormIndex ancestorIndex = null;
    		FormIndex cur = null;
    		FormIndex qcur = questionIndex;
    		for (int i = 0; i < defs.size() - 1; i++) {
    			FormIndex next = new FormIndex(qcur.getLocalIndex(), qcur.getInstanceIndex());
    			if (ancestorIndex == null) {
    				ancestorIndex = next;
    				cur = next;
    			} else {
    				cur.setNextLevel(next);
    				cur = next;
    			}
    			qcur = qcur.getNextLevel();
    		
            	TreeElement ancestorNode = form.getDataModel().resolveReference(form.getChildInstanceRef(ancestorIndex));
            	if (!ancestorNode.isRelevant()) {
            		relevant = false;
            		break;
            	}
    		}
    	}
        	
   		return relevant;
   	}

    public void registerObservable (FormEntryModelListener feml) {
		if (!observers.contains(feml)) {
			observers.addElement(feml);
		}
    }

    public void unregisterObservable (FormEntryModelListener feml) {
		observers.removeElement(feml);
    }
    
	public void unregisterAll () {
		observers.removeAllElements();
	}
	
    /**
	 * @return Whether or not the form model should be written to.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly Whether or not the form model should be changed by the
	 * form entry interaction.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return the startIndex
	 */
	public FormIndex getStartIndex() {
		return startIndex;
	}
}