package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.clforms.widgets.DateQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.NumericQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.Select1QuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.SelectQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TextQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TimeQuestionWidget;

public class FormViewManager implements IFormEntryView, FormEntryModelListener, CommandListener, ItemCommandListener
{
	private FormEntryController controller;
	private FormEntryModel model;
	private FormViewScreen parent;

	private int index;
	private QuestionDef prompt;
	private IAnswerData answer;
	private SingleQuestionScreen widget;
	// GUI elements
	public FormViewManager(String formTitle, FormEntryModel model, FormEntryController controller, int questionIndex, FormViewScreen node)
	{

	   	this.parent = node;
		this.model = model;
    	this.controller = controller;
		//immediately setup question, need to decide if this is the best place to do it
    	this.getView(questionIndex);
    	//controller.setView(this);
    	model.registerObservable(this);
	}

	public int getIndex()
	{
		index = model.getQuestionIndex();//return index of active question
		return index;
	}

	public void getView(int qIndex)
	{

		prompt = model.getQuestion(qIndex);
		//checks question type
		int qType = prompt.getDataType();
		int contType = prompt.getControlType();

		switch(contType){
		case Constants.CONTROL_INPUT:
			switch (qType)
			{
			case Constants.DATATYPE_TEXT:
				//go to TextQuestion Widget
				widget = new TextQuestionWidget(prompt);
				widget.setCommandListener(this);
				widget.setItemCommandListner(this);
				controller.setDisplay(widget);
				break;
			case Constants.DATATYPE_DATE:
				//go to DateQuestion Widget
				widget = new DateQuestionWidget(prompt);
				widget.setCommandListener(this);
				widget.setItemCommandListner(this);
				controller.setDisplay(widget);
				break;
			case Constants.DATATYPE_TIME:
				//go to TimeQuestion Widget
				widget = new TimeQuestionWidget(prompt);
				widget.setCommandListener(this);
				widget.setItemCommandListner(this);
				controller.setDisplay(widget);
				break;
			case Constants.DATATYPE_INTEGER:
				widget = new NumericQuestionWidget(prompt);
				widget.setCommandListener(this);
				widget.setItemCommandListner(this);
				controller.setDisplay(widget);
				break;
			}
			break;
		case Constants.CONTROL_SELECT_ONE:
			//go to SelectQuestion widget
			widget = new Select1QuestionWidget(prompt);
			widget.setCommandListener(this);
			widget.setItemCommandListner(this);
			controller.setDisplay(widget);
			break;
		case Constants.CONTROL_SELECT_MULTI:
			//go to SelectQuestion Widget
			widget = new SelectQuestionWidget(prompt);
			widget.setCommandListener(this);
			widget.setItemCommandListner(this);
			controller.setDisplay(widget);
			break;
		case Constants.CONTROL_TEXTAREA:
			//go to TextQuestion Widget
			widget = new TextQuestionWidget(prompt);
			widget.setCommandListener(this);
			widget.setItemCommandListner(this);
			controller.setDisplay(widget);
			break;
		default:
			System.out.println("Unsupported type!");
			break;
		}
	}


	public void destroy() {
		model.unregisterObservable(this);

	}


	public void setContext(FormEntryContext context) {
		// TODO Auto-generated method stub

	}


	public void show() {
		getView(getIndex());//refresh view
	}

	public void refreshView()
	{
		getView(getIndex());//refresh view
	}

	public void formComplete() {

	  try {
		   Thread.sleep(1000);
		  } catch (InterruptedException ie) { }

		controller.save();//always save form
		controller.exit();

	}


	public void questionIndexChanged(int questionIndex) {
		if (questionIndex != -1)
			getView(getIndex());//refresh view
	}


	public void saveStateChanged(int instanceID, boolean dirty) {
		// TODO Auto-generated method stub

	}

	public void commandAction(Command command, Displayable arg1)
	{
		if (command == SingleQuestionScreen.nextItemCommand || command == SingleQuestionScreen.nextCommand) {
			answer=widget.getWidgetValue();

				//System.out.println("you answered "+ answer.getDisplayText()+" for "+prompt.getLongText()+" moving on");

				if(prompt.isRequired() && answer == null)
				{
					Alert alert = new Alert("Question Required", "Question Required!!!", null, AlertType.ERROR);
					controller.setDisplay(alert);
				}
				else{
				//save and proceed to next question
					controller.commitAnswer(this.prompt, answer);
					if(model.getQuestionIndex()+1 < model.getNumQuestions() )
					{
						controller.stepQuestion(true);
					}
					else{
					   controller.save();//always save
				       parent.show();
					//parent.show();
//					formComplete();//go to form view list instead
//						model.setFormComplete();
					}
				}
		}
		else if (command == SingleQuestionScreen.previousCommand) {
			controller.stepQuestion(false);
			refreshView();
		}
		else if (command == SingleQuestionScreen.viewAnswersCommand){
			controller.save();//always save
			 parent.show();
		}
	}

    public void commandAction(Command c, Item item)
    {
    	if(c == SingleQuestionScreen.nextItemCommand)
      {
			answer=widget.getWidgetValue();
			//System.out.println("you answered "+ answer.getDisplayText()+" for "+prompt.getLongText()+" moving on");
			controller.questionAnswered(this.prompt, answer);//store answers
			refreshView();

      }
    }
}