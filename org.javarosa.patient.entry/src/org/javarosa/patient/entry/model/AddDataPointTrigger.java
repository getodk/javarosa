package org.javarosa.patient.entry.model;

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.model.xform.XPathReference;

public class AddDataPointTrigger implements ITriggerHandler {

	FormDef targetForm;
	String pathRoot;
	QuestionDataGroup modelRoot;
	
	int currentFormIndex;
	
	int currentIndex;
	
	public void handle() {
		XPathReference valueRef = new XPathReference(pathRoot + "Value" + currentIndex);
		XPathReference dateRef = new XPathReference(pathRoot + "Date" + currentIndex);
		XPathReference moreRef = new XPathReference(pathRoot + "More" + currentIndex);
		
		QuestionDataElement value = new QuestionDataElement("Value", valueRef);
		QuestionDataElement date = new QuestionDataElement("Date", dateRef);
		TriggerQuestionDataElement moreValues = new TriggerQuestionDataElement();
		
		moreValues.setName("MoreVals");
		moreValues.setReference(moreRef);

		AddDataPointTrigger newTrigger = new AddDataPointTrigger();
		newTrigger.setModelRoot(modelRoot);
		newTrigger.setPathRoot(pathRoot);
		newTrigger.setTargetForm(targetForm);
		newTrigger.setCurrentIndex(currentIndex + 1);
		newTrigger.setCurrentFormIndex(currentFormIndex + 3);
		
		moreValues.setTriggerHandler(newTrigger);
		
		modelRoot.addChild(value);
		modelRoot.addChild(date);
		modelRoot.addChild(moreValues);
		
		QuestionDef valueDef = new QuestionDef();
		valueDef.setLongText("Measurement Value");
		valueDef.setShortText("Value");
		valueDef.setDataType(Constants.DATATYPE_INTEGER);
		valueDef.setBind(valueRef);
		
		QuestionDef dateDef = new QuestionDef();
		dateDef.setLongText("Measurement Date");
		dateDef.setShortText("Date");
		dateDef.setDataType(Constants.DATATYPE_DATE);
		dateDef.setBind(dateRef);
		
		QuestionDef moreDef = new QuestionDef();
		moreDef.setLongText("Do you have more data points to enter?");
		moreDef.setShortText("More Points");
		moreDef.setDataType(Constants.DATATYPE_LIST_EXCLUSIVE);
		moreDef.setControlType(Constants.CONTROL_SELECT_ONE);
		moreDef.setBind(moreRef);
		moreDef.addSelectItem("Yes", "Yes");
		moreDef.addSelectItem("No","No");
		
		Vector formChildren = targetForm.getChildren();
		
		formChildren.insertElementAt(moreDef, currentFormIndex);
		formChildren.insertElementAt(dateDef, currentFormIndex);
		formChildren.insertElementAt(valueDef, currentFormIndex);
		
		targetForm.setChildren(formChildren);
	}
	
	/**
	 * @return the currentIndex
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * @param currentIndex the currentIndex to set
	 */
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	/**
	 * @return the targetForm
	 */
	public FormDef getTargetForm() {
		return targetForm;
	}

	/**
	 * @param targetForm the targetForm to set
	 */
	public void setTargetForm(FormDef targetForm) {
		this.targetForm = targetForm;
	}

	/**
	 * @return the pathRoot
	 */
	public String getPathRoot() {
		return pathRoot;
	}

	/**
	 * @param pathRoot the pathRoot to set
	 */
	public void setPathRoot(String pathRoot) {
		this.pathRoot = pathRoot;
	}

	/**
	 * @return the modelRoot
	 */
	public QuestionDataGroup getModelRoot() {
		return modelRoot;
	}

	/**
	 * @param modelRoot the modelRoot to set
	 */
	public void setModelRoot(QuestionDataGroup modelRoot) {
		this.modelRoot = modelRoot;
	}

	/**
	 * @return the currentFormIndex
	 */
	public int getCurrentFormIndex() {
		return currentFormIndex;
	}

	/**
	 * @param currentFormIndex the currentFormIndex to set
	 */
	public void setCurrentFormIndex(int currentFormIndex) {
		this.currentFormIndex = currentFormIndex;
	}
}
