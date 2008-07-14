package org.javarosa.formmanager.controller;

import org.javarosa.formmanager.model.*;
import org.javarosa.formmanager.model.temp.*;
import org.javarosa.formmanager.view.*;

public class FormEntryController {
	public static final int QUESTION_OK = 0;
	public static final int QUESTION_REQUIRED_BUT_EMPTY = 1;
	
	FormEntryModel model;
	IFormEntryView view;
	
	public FormEntryController (FormEntryModel model) {
		this.model = model;
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
	
	public void stepQuestion (boolean next) {
		selectQuestion(model.getQuestionIndex() + (next ? 1 : -1)); // +/- 1 won't work; need to find next *relevant* question
	}
	
	public void selectQuestion (int questionIndex) {
		
	}
	
	public void save () {
		
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