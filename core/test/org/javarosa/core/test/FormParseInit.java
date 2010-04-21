package org.javarosa.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.test.FormDefTest;
import org.javarosa.core.model.test.QuestionDefTest;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.util.XFormUtils;

/* TODO
 * Priority: Top priority is getting the localizations tested so that test coverage isn't lost
 * 			Have a method to provide answers to test constraints
 */


/**
 * This class sets up everything you need to perform tests on the models and form elements found in JR (such
 * as QuestionDef, FormDef, Selections, etc).  It exposes hooks to the FormEntryController,FormEntryModel and
 * FormDef (all the toys you need to test IFormElements, provide answers to questions and test constraints, etc)
 * 
 * REMEMBER to set the 
 */

public class FormParseInit {
	private String FORM_NAME = new String("resources/ImageSelectTester.xhtml");
	private FormDef xform;
	private FormEntryController fec;
	private FormEntryModel femodel;

	
	public FormParseInit(){
		this.init();
	}
	
	/**
	 * Set a new form to be parsed (instead of the default), and calls
	 * init() immediately to parse it.  It is uneccessary to call init() again after
	 * using this method.
	 * @param uri URI pointing to the xform.
	 */
	public void setFormToParse(String uri){
		FORM_NAME = uri;
		this.init();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		FormParseInit t = new FormParseInit();
		
//		System.out.println("Form Summary");
//		System.out.println(t.printStuff());
//		
//		System.out.println("Stepping through questions...");
//		
//		t.fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());
//		do{
//			QuestionDef q = t.getCurrentQuestion();
//			if(q!=null)	System.out.println("Question Text ID is: "+q.getTextID());
//		}while(t.fec.stepToNextEvent()!=FormEntryController.EVENT_END_OF_FORM);
		for(int i=0;i<QuestionDefTest.NUM_TESTS+1;i++){
			new QuestionDefTest().doTest(i);
		}
		
		for(int i=0;i<FormDefTest.NUM_TESTS+1;i++){
			new FormDefTest().doTest(i);
		}
		
	}
	
	public void init(){
		String xf_name = FORM_NAME; 			
		FileInputStream is;
		try {
			is = new FileInputStream(xf_name);
		} catch (FileNotFoundException e) {
			System.err.println("Error: the file '" + xf_name
					+ "' could not be found!");
			throw new RuntimeException("Error: the file '" + xf_name
					+ "' could not be found!");
		}
		
		// Parse the form
		xform = XFormUtils.getFormFromInputStream(is);
		
		femodel = new FormEntryModel(xform);
		fec = new FormEntryController(femodel);
		
		if( xform == null ) {
			System.out.println("\n\n==================================\nERROR: XForm has failed validation!!");
		} else {
		}
	}
	

	/**
	 * @return the first questionDef found in the form.
	 */
	public QuestionDef getFirstQuestionDef(){
		//go to the beginning of the form
		fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		do{
			FormEntryCaption fep = femodel.getCaptionPrompt();
			if(fep.getFormElement() instanceof QuestionDef){
				return (QuestionDef)fep.getFormElement();
			}
		}while(fec.stepToNextEvent()!=FormEntryController.EVENT_END_OF_FORM);
		
		return null; //we should really never end up here.
	}
	
	/**
	 * Gets the current question based off of
	 * @return the question after getFirstQuestionDef()
	 */
	public QuestionDef getCurrentQuestion(){
		FormEntryCaption fep = femodel.getCaptionPrompt();
		if(fep.getFormElement() instanceof QuestionDef){
			return (QuestionDef)fep.getFormElement();
		}		
		return null;
	}
	
	/**
	 * 
	 * @return the next question in the form (QuestionDef), or null if the end of the form has been reached.
	 */
	public QuestionDef getNextQuestion(){
		//jump to next event and check for end of form
		if(fec.stepToNextEvent() == FormEntryController.EVENT_END_OF_FORM) return null;
		
		FormEntryCaption fep = this.getFormEntryModel().getCaptionPrompt();
		
		do{
			if(fep.getFormElement() instanceof QuestionDef) return (QuestionDef)fep.getFormElement();
		}while(fec.stepToNextEvent()!=FormEntryController.EVENT_END_OF_FORM);
		
		return null;
	}
	
	/**
	 * @return the FormDef for this form
	 */
	
	public FormDef getFormDef(){
		return xform;
	}
	
	public FormEntryModel getFormEntryModel(){
		return fec.getModel();
	}
	
	public FormEntryController getFormEntryController(){
		return fec;
	}
	
	/*
	 * Makes an 'extremely basic' print out of the xform model.
	 */
	public String printStuff(){
		String stuff = "";
		//go to the beginning of the form
		fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		do{
			FormEntryCaption fep = femodel.getCaptionPrompt();
			boolean choiceFlag = false;
			
			if(fep.getFormElement() instanceof QuestionDef){
				stuff+="\t[Type:QuestionDef, ";
				Vector s = ((QuestionDef)fep.getFormElement()).getChoices();
				stuff+="ContainsChoices: "+ ((s != null && s.size() > 0) ? "true " : "false" ) +", ";
				if(s != null && s.size() > 0) choiceFlag = true;
			}else if(fep.getFormElement() instanceof FormDef){
				stuff+="\t[Type:FormDef, ";
			}else if(fep.getFormElement() instanceof GroupDef){
				stuff+="\t[Type:GroupDef, ";
			}else{
				stuff+="\t[Type:Unknown]\n";
				continue;
			}
			
			stuff+="ID:"+fep.getFormElement().getID()+", TextID:"+fep.getFormElement().getTextID()+",InnerText:"+fep.getFormElement().getLabelInnerText();
			if(choiceFlag){
				
				stuff+="] \n\t\t---Choices:"+((QuestionDef)fep.getFormElement()).getChoices().toString()+"\n";
			}else{
				stuff+="]\n";
			}
		}while(fec.stepToNextEvent()!=fec.EVENT_END_OF_FORM);
		
		return stuff;
	}
	

}
