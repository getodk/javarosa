package org.javarosa.formmanager.controller;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.IFormEntryView;

public class FormEntryController {
	public static final int QUESTION_OK = 0;
	public static final int QUESTION_REQUIRED_BUT_EMPTY = 1;
	
	FormEntryModel model;
	IFormEntryView view;
	IControllerHost parent;
	
	public FormEntryController (FormEntryModel model, IControllerHost parent) {
		this.model = model;
		this.parent = parent;
	}
	
	public void setView (IFormEntryView view) {
		this.view = view;
	}
	
	public int questionAnswered (QuestionDef question, IAnswerData data) {
		if (question.isRequired() && data == null) {
			return QUESTION_REQUIRED_BUT_EMPTY;
		} else {
			if (data != null || model.getForm().getValue(question) != null) {
				//we should check if the data to be saved is already the same as the data in the model, but we can't
				model.getForm().setValue(question, data);
				model.modelChanged();
			}
			
			stepQuestion(true);
			return QUESTION_OK;
		}
	}
	
	public void stepQuestion (boolean forward) {
		int inc = (forward ? 1 : -1);
		int index = model.getQuestionIndex();
		
		do {
			index += inc;
		} while (index >= 0 && index < model.getNumQuestions() && !model.getQuestion(index).isVisible());
		
		if (index < 0) {
			//already at the earliest relevant question
			return;
		} else if (index >= model.getNumQuestions()) {
			model.setFormComplete();
			return;
		}
		
		selectQuestion(index);
	}
	
	public void selectQuestion (int questionIndex) {
		model.setQuestionIndex(questionIndex);
	}
	
	public void save () {
		boolean postProcessModified = model.getForm().postProcessModel();
		
		if (!model.isSaved() || postProcessModified) {
			FormDef form = model.getForm();
			DataModelTreeRMSUtility utility = (DataModelTreeRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(DataModelTreeRMSUtility.getUtilityName());
			DataModelTree instance = (DataModelTree)form.getDataModel(); //worry about supporting other data model types later
			int instanceID = model.getInstanceID();
			
			instance.setName(form.getName());
	        instance.setFormReferenceId(form.getRecordId());
	        instance.setDateSaved(new Date());
			
			if(instanceID == -1) {
				instanceID = utility.writeToRMS(instance);
			} else {
				utility.updateToRMS(instanceID, instance);
			}			
		
			model.modelSaved(instanceID);
		}
	}
	
	public void exit () {
		view.destroy();
		parent.controllerReturn("exit");
	}
	
	public void startOver () {
		
	}
	
	public void setLanguage (String language) {
		model.getForm().getLocalizer().setLocale(language);
	}
	
	public void cycleLanguage () {
		setLanguage(model.getForm().getLocalizer().getNextLocale());
	}
	
	public void setDisplay (Displayable d) {
		parent.setDisplay(d);
	}
}