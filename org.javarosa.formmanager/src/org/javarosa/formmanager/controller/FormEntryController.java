package org.javarosa.formmanager.controller;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.utils.Localizer;
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
			if (data != null) {
				model.getForm().setValue(question, data);
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
			model.setQuestionIndex(-1);
			view.formComplete();

			System.out.println("form done!");
			return;
		}
		
		selectQuestion(index);
	}
	
	public void selectQuestion (int questionIndex) {
		model.setQuestionIndex(questionIndex);
	}
	
	public void save () {
		//do form post-processing here
	}
	
	public void exit () {
		view.destroy(); //?
		parent.controllerReturn("all done!");
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