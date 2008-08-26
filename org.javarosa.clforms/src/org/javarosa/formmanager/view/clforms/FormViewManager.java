package org.javarosa.formmanager.view.clforms;

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
		
	System.out.println("Receiving :"+qType+" and "+contType);
	System.out.println("Match Type :"+Constants.DATATYPE_LIST_EXCLUSIVE);

		//obtains correct view

		switch(contType){
		case Constants.CONTROL_INPUT:
			switch (qType)
			{
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
/*			default:
				System.out.println("Unsupported type!");
				break;*/
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
		getView(getIndex());//refresh view
	}


	public void saveStateChanged(int instanceID, boolean dirty) {
		// TODO Auto-generated method stub

	}

	public void commandAction(Command command, Displayable arg1)
	{
		if (command == SingleQuestionScreen.nextItemCommand) {
				answer=widget.getWidgetValue();
				//System.out.println("you answered "+ answer.getDisplayText()+" for "+prompt.getLongText()+" moving on");
				controller.questionAnswered(this.prompt, answer);//store answers
//				refreshView();
				}

		else if (command == SingleQuestionScreen.previousCommand) {
			controller.stepQuestion(false);
			refreshView();
			//parent.show();
		}
		else if (command == SingleQuestionScreen.viewAnswersCommand){
			controller.save();//always save
			//controller.exit();
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

/*	public void addNavigationButtons()
	{
		StringItem backItem = new StringItem(null,"BACK",Item.BUTTON);
		StringItem nextItem = new StringItem(null,"NEXT",Item.BUTTON);

		this.append(nextItem);
	    //this.append(backItem);//disable, handled by previousCommand

	    backItem.setDefaultCommand(backItemCommand);     // add Command to Item.
	    backItem.setItemCommandListener(this);       // set item command listener
	    nextItem.setDefaultCommand(nextItemCommand);     // add Command to Item.
	    nextItem.setItemCommandListener(this);       // set item command listener

	}*/
}