package org.javarosa.core.test;

import static org.javarosa.test.utils.ResourcePathHelper.r;

import java.nio.file.Path;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* TODO
 * Priority: Top priority is getting the localizations tested so that test coverage isn't lost
 *             Have a method to provide answers to test constraints
 */


/**
 * This class sets up everything you need to perform tests on the models and form elements found in JR (such
 * as QuestionDef, FormDef, Selections, etc).  It exposes hooks to the FormEntryController,FormEntryModel and
 * FormDef (all the toys you need to test IFormElements, provide answers to questions and test constraints, etc)
 *
 * REMEMBER to set the
 */


//TODO
//Make some better lazy-people methods for testing constraints.


public class FormParseInit {
    private static final Logger logger = LoggerFactory.getLogger(FormParseInit.class);
    private final String FORM_NAME;
    private FormDef xform;
    private FormEntryController fec;
    private FormEntryModel femodel;


    public FormParseInit(){
        FORM_NAME = r("ImageSelectTester.xhtml").toString();
        this.init();
    }

    public FormParseInit(Path form){
        FORM_NAME = form.toString();
        this.init();
    }

    private void init(){
        String xf_name = FORM_NAME;
        FileInputStream is;
        try {
            is = new FileInputStream(xf_name);
        } catch (FileNotFoundException e) {
            logger.error("Error: the file '{}' could not be found!", xf_name);
            throw new RuntimeException("Error: the file '" + xf_name + "' could not be found!");
        }

        // Parse the form
        xform = XFormUtils.getFormFromInputStream(is);

        femodel = new FormEntryModel(xform);
        fec = new FormEntryController(femodel);

        if( xform == null ) {
            logger.error("ERROR: XForm has failed validation!!");
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

        return null;
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
                List<SelectChoice> s = ((QuestionDef)fep.getFormElement()).getChoices();
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
