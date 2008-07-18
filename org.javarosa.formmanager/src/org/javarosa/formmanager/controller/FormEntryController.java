package org.javarosa.formmanager.controller;

import org.javarosa.clforms.api.Prompt;
import org.javarosa.core.model.QuestionData;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.model.IControllerListener;
import org.javarosa.formmanager.view.IFormEntryView;

public class FormEntryController {
	public static final int QUESTION_OK = 0;
	public static final int QUESTION_REQUIRED_BUT_EMPTY = 1;
	
	FormEntryModel model;
	IFormEntryView view;
	
	IControllerListener listener;
	
	public FormEntryController (FormEntryModel model, IControllerListener listener) {
		this.model = model;
		this.listener = listener;
	}
	
	public void setView (IFormEntryView view) {
		this.view = view;
	}
	
	public int questionAnswered (Prompt question, QuestionData data) {
		if (true /* check data sufficient to answer question (specifically, required attribute) */) {
			return QUESTION_REQUIRED_BUT_EMPTY;
		} else {
			//model.updatequestion...;
			stepQuestion(true);
			return QUESTION_OK;
		}
	}
	
	public void stepQuestion (boolean forward) {
		selectQuestion(model.getQuestionIndex() + (forward ? 1 : -1)); // +/- 1 won't work; need to find next *relevant* question
	}
	
	public void selectQuestion (int questionIndex) {
		
	}
	
	public void save () {
		//do form post-processing here
	}
	
	public void exit () {
		view.destroy(); //?
	}
	
	public void startOver () {
		
	}
	
	public void setLanguage (String language) {
		
	}
	
	public void cycleLanguage () {
		
	}
}