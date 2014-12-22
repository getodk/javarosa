/**
 * 
 */
package org.javarosa.xform.schema;

import java.io.InputStream;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;

/**
 * @author ctsims
 *
 */
public class FormInstanceValidator {
	FormDef theForm;
	FormInstance savedModel;
	
	FormEntryModel model;
	FormEntryController controller;
	
	public FormInstanceValidator(InputStream formInput, InputStream instanceInput) throws Exception {
		theForm = XFormUtils.getFormFromInputStream(formInput, FormDef.latestImplementationMode);
		
		savedModel = XFormParser.restoreDataModel(instanceInput, null);
        TreeElement templateRoot = theForm.getInstance().getRoot().deepCopy(true);

        //sanity check instance names before loading
        if (!savedModel.getRoot().getName().equals(templateRoot.getName()) || savedModel.getRoot().getMult() != 0) {
			System.out.println("Instance model name does not match xform instance name.");
			System.out.println("Instance: " + savedModel.getName() + " Xform: " + templateRoot.getName());
			System.exit(1);
        }
        
        model = new FormEntryModel(theForm);
        controller = new FormEntryController(model);

        
        //Populate XForm Model 
//        TreeReference tr = TreeReference.rootRef();
//        tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
//        templateRoot.populate(savedRoot, f);
//
//        f.getInstance().setRoot(templateRoot);
	}
	
	public void simulateEntryTest() throws Exception {
    	while(model.getEvent() != FormEntryController.EVENT_END_OF_FORM) {
    		int event = controller.stepToNextEvent();
    		deal(event);
    	}
	}
	
	private void deal(int event) throws Exception {
		switch(event) {
		//Events which we can just skip
		case FormEntryController.EVENT_END_OF_FORM:
		case FormEntryController.EVENT_BEGINNING_OF_FORM:
		case FormEntryController.EVENT_GROUP:
		case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
		case FormEntryController.EVENT_REPEAT:
		case FormEntryController.EVENT_REPEAT_JUNCTURE:
			return;
			
		case FormEntryController.EVENT_QUESTION:
			//Get the index for this question
			FormIndex index = model.getFormIndex();
			
			TreeReference ref = theForm.getChildInstanceRef(index);
			
			TreeElement element = savedModel.resolveReference(ref);
			
			FormEntryPrompt prompt = model.getQuestionPrompt(index);
			
			if(element == null) {
				//If an element doesn't exist, that is indicative that its model was not relevant,
				//so the question shouldn't have been asked in the first place
				System.out.println("Saved model is missing valid node for question " + ref.toString(true));
				throw new Exception("Model Missing Element");
			}
			
			IAnswerData answerValue = element.getValue();
			
			answerValue = cast(answerValue, prompt.getDataType());
			
			int result = controller.answerQuestion(index, answerValue, false, true);
			switch(result) {
			case FormEntryController.ANSWER_OK:
				break;
			case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
				//The form did not contain a value for a question which was required
				System.out.println("Saved model is missing a value in a required question " + ref.toString(true));
				throw new Exception("Model Missing Values");
			case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
				//The input to this question wasn't valid
				System.out.println("Saved model's data violates a constraint for node " + ref.toString(true));
				System.out.println("Constraint Message: " + prompt.getConstraintText(answerValue));
				throw new Exception("Invalid data");
			}
			
			//One more thing to evalute: multi/single select values are in the valid range.
			Vector<SelectChoice> choices = prompt.getSelectChoices();
			if(choices ==null) {
				//Not the right kind of question!
				return;
			}
			
			//Otherwise we need to make sure that any available IAnswerData's are legit.
			
			if(answerValue == null) {
				//Would have caught required stuff earlier, and null is always valid
				//unless the question is required, so we're fine
				return;
			}
			
			//Check for one or multi
			if (prompt.getControlType() == Constants.CONTROL_SELECT_MULTI) {
				SelectMultiData data = new SelectMultiData().cast(answerValue.uncast());
				Vector<Selection> values = (Vector<Selection>)data.getValue();
				for(Selection selection : values) {
					if(!(validChoice(selection.xmlValue, choices))) {
						System.out.println("Selection contains an invalid value [" + selection.xmlValue + "] at node " + ref.toString(true));
						throw new Exception("Invalid Selection");
					}
				}
				
    		} else {
    			SelectOneData data = new SelectOneData().cast(answerValue.uncast());
				if(!(validChoice(((Selection)data.getValue()).xmlValue, choices))) {
					System.out.println("Selection contains an invalid value [" + ((Selection)data.getValue()).xmlValue + "] at node " + ref.toString(true));
					throw new Exception("Invalid Selection");
				}
    		}
		}
	}
	
	public boolean validChoice(String choice, Vector<SelectChoice> choices) {
		for(SelectChoice c : choices) {
			if(c.getValue().equals(choice)) {return true;}
		}
		return false;
	}
	
	/**
	 * Duplicated everywhere I'm sure.
	 * @param data
	 * @param dataType
	 * @return
	 */
	private IAnswerData cast(IAnswerData data, int dataType) {
		if(data == null) { return null; }
		
		IAnswerData prototype = new UncastData();
		switch(dataType) {
		case Constants.DATATYPE_DATE:
			prototype = new DateData();
			break;
		case Constants.DATATYPE_DATE_TIME:
			prototype = new DateTimeData();
			break;
		case Constants.DATATYPE_TIME:
			prototype = new TimeData();
			break;
		case Constants.DATATYPE_INTEGER:
			prototype = new IntegerData();
			break;
		case Constants.DATATYPE_LONG:
			prototype = new LongData();
			break;
		case Constants.DATATYPE_DECIMAL:
			prototype = new DecimalData();
			break;
		}
		return prototype.cast(data.uncast());
	}
}
