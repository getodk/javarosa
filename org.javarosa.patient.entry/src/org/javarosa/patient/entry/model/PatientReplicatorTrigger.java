package org.javarosa.patient.entry.model;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.model.xform.XPathReference;

public class PatientReplicatorTrigger implements ITriggerHandler {

	FormDef targetForm;
	
	int currentElement = 0;
	
	public void handle() {
		createPatientQuestionsOnForm();
	}
	
	public void createPatientQuestionsOnForm() {
		String rootRef = "/Patients/Patient" + currentElement + "/";
		
		QuestionDef givenName = new QuestionDef();
		givenName.setLongText("Patient's Given Name");
		givenName.setShortText("Given Name");
		givenName.setDataType(Constants.DATATYPE_TEXT);
		givenName.setBind(new XPathReference(rootRef + "GivenName"));
		
		QuestionDef lastName = new QuestionDef();
		lastName.setLongText("Patient's Last Name");
		lastName.setShortText("Last Name");
		lastName.setDataType(Constants.DATATYPE_TEXT);
		lastName.setBind(new XPathReference(rootRef + "FamilyName"));
		
		QuestionDef middleName = new QuestionDef();
		middleName.setLongText("Patient's Middle Name");
		middleName.setShortText("Middle Name");
		middleName.setDataType(Constants.DATATYPE_TEXT);
		middleName.setBind(new XPathReference(rootRef + "MiddleName"));
		
		QuestionDef DOB = new QuestionDef();
		DOB.setLongText("Patient's Date of Birth");
		DOB.setShortText("Date of Birth");
		DOB.setDataType(Constants.DATATYPE_DATE);
		DOB.setBind(new XPathReference(rootRef + "DOB"));

		QuestionDef treatmentStart = new QuestionDef();
		treatmentStart.setLongText("Start Date of Patient's Treamtment");
		treatmentStart.setShortText("Start Date");
		treatmentStart.setDataType(Constants.DATATYPE_DATE);
		treatmentStart.setBind(new XPathReference(rootRef + "TreatmentStartDate"));
		
		XPathReference weightRef = new XPathReference(rootRef + "WeightValues");
		
		QuestionDef weightPoints = new QuestionDef();
		weightPoints.setLongText("Do you have weight measurements to enter?");
		weightPoints.setShortText("More Points");
		weightPoints.setDataType(Constants.DATATYPE_LIST_EXCLUSIVE);
		weightPoints.setControlType(Constants.CONTROL_SELECT_ONE);
		weightPoints.setBind(weightRef);
		weightPoints.addSelectItem("Yes", "Yes");
		weightPoints.addSelectItem("No","No");
		
		XPathReference moreRef = new XPathReference(rootRef + "MorePatients");
		
		QuestionDef morePatients = new QuestionDef();
		morePatients.setLongText("Are there more patients to enter?");
		morePatients.setShortText("More Patients");
		morePatients.setDataType(Constants.DATATYPE_LIST_EXCLUSIVE);
		morePatients.setControlType(Constants.CONTROL_SELECT_ONE);
		morePatients.setBind(moreRef);
		morePatients.addSelectItem("Yes", "Yes");
		morePatients.addSelectItem("No","No");
		
		targetForm.addChild(givenName);
		targetForm.addChild(lastName);
		targetForm.addChild(middleName);
		targetForm.addChild(DOB);
		targetForm.addChild(treatmentStart);
		targetForm.addChild(weightPoints);
		targetForm.addChild(morePatients);
		addPatientToDataModel();
	}
	
	public void addPatientToDataModel() {
		DataModelTree tree = (DataModelTree)targetForm.getDataModel();
		QuestionDataGroup root = (QuestionDataGroup)tree.getRootElement();
		String rootRef = "/Patients/Patient" + currentElement + "/";
		
		QuestionDataGroup patientNode = new QuestionDataGroup("Patient" + currentElement);
		
		QuestionDataElement givenName = new QuestionDataElement("GivenName", new XPathReference(rootRef + "GivenName"));
		QuestionDataElement familyName = new QuestionDataElement("FamilyName", new XPathReference(rootRef + "FamilyName"));
		QuestionDataElement middleName = new QuestionDataElement("MiddleName", new XPathReference(rootRef + "MiddleName"));
		
		QuestionDataElement dateOfBirth = new QuestionDataElement("DOB", new XPathReference(rootRef + "DOB"));
		QuestionDataElement treatmentStartDate = new QuestionDataElement("TreatmentStartDate", new XPathReference(rootRef + "TreatmentStartDate"));
		
		XPathReference weightValuesReference = new XPathReference(rootRef + "WeightValues");
		XPathReference morePatientsReference = new XPathReference(rootRef + "MorePatients");
		
		TriggerQuestionDataElement weightValues = new TriggerQuestionDataElement();
		weightValues.setName("WeightValues");
		weightValues.setReference(weightValuesReference);
		
		QuestionDataGroup weightGroup = new QuestionDataGroup("Weight");
		String weightRef = rootRef + "Weight/";

		AddDataPointTrigger newTrigger = new AddDataPointTrigger();
		newTrigger.setModelRoot(weightGroup);
		newTrigger.setPathRoot(weightRef);
		newTrigger.setTargetForm(targetForm);
		newTrigger.setCurrentIndex(0);
		newTrigger.setCurrentFormIndex(targetForm.getChildren().size() -1);
		
		weightValues.setTriggerHandler(newTrigger);
		
		patientNode.addChild(givenName);
		patientNode.addChild(familyName);
		patientNode.addChild(middleName);
		
		patientNode.addChild(dateOfBirth);
		patientNode.addChild(treatmentStartDate);
		patientNode.addChild(weightGroup);
		patientNode.addChild(weightValues);
		
		TriggerQuestionDataElement morePatients = new TriggerQuestionDataElement();
		morePatients.setName("MorePatients");
		
		PatientReplicatorTrigger patTrigger = new PatientReplicatorTrigger();
		patTrigger.setTargetForm(targetForm);
		patTrigger.setCurrentElement(currentElement + 1);
		
		morePatients.setTriggerHandler(patTrigger);
		morePatients.setReference(morePatientsReference);
		
		patientNode.addChild(morePatients);
		
		root.addChild(patientNode);
	}

	/**
	 * @return the currentElement
	 */
	public int getCurrentElement() {
		return currentElement;
	}

	/**
	 * @param currentElement the currentElement to set
	 */
	public void setCurrentElement(int currentElement) {
		this.currentElement = currentElement;
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
}
