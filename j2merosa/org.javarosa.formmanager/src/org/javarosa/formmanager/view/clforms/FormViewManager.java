package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;

import org.javarosa.core.api.IView;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.FormElementBinding;
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

	private FormIndex index;
	private FormElementBinding prompt;
	private IAnswerData answer;
	private SingleQuestionScreen widget;
	Gauge progressBar;
	private boolean showFormView;
	private FormViewScreen formView;
	private boolean direction ;
	// GUI elements
	public FormViewManager(String formTitle, FormEntryModel model, FormEntryController controller)
	{
		this.model = model;
    	this.controller = controller;
    	this.parent = new FormViewScreen(model);
    	this.showFormView = true;
    	model.registerObservable(this);
//immediately setup question, need to decide if this is the best place to do it
//    	this.getView(questionIndex);
    	controller.setFormEntryView(this);

	}

	public FormIndex getIndex()
	{
		index = model.getQuestionIndex();//return index of active question
		return index;
	}

	public void getView(FormIndex qIndex, boolean fromFormView )
	{
		prompt = new FormElementBinding(null, qIndex, model.getForm());
		//checks question type
		int qType = prompt.instanceNode.dataType;
		int contType = ((QuestionDef)prompt.element).getControlType();

		switch(contType){
		case Constants.CONTROL_INPUT:
			switch (qType)
			{
			case Constants.DATATYPE_TEXT:
			case Constants.DATATYPE_NULL:
			case Constants.DATATYPE_UNSUPPORTED:
				//go to TextQuestion Widget
				if (fromFormView == true)					
					widget = new TextQuestionWidget(prompt,'c');
				else
					if (direction )
						widget = new TextQuestionWidget(prompt,1);
					else 
						widget = new TextQuestionWidget(prompt,"");
				break;
			case Constants.DATATYPE_DATE:
				//go to DateQuestion Widget
				if (fromFormView == true)
					widget = new DateQuestionWidget(prompt,'c');//transition must be fromAnswerScreen style
				else
					if (direction == true)
						widget = new DateQuestionWidget(prompt,1);//transition = next  style
					else
						widget = new DateQuestionWidget(prompt,""); //transition = back style
				break;
			case Constants.DATATYPE_TIME:
				//go to TimeQuestion Widget
				if (fromFormView == true)
					widget = new TimeQuestionWidget(prompt,'c');
				else	
				if (direction == true)
					widget = new TimeQuestionWidget(prompt,1);
				else 
					widget = new TimeQuestionWidget(prompt,"");
				break;
			case Constants.DATATYPE_INTEGER:
				if (fromFormView == true)
					widget = new NumericQuestionWidget(prompt,'c');
				else
					if (direction == true)
						widget = new NumericQuestionWidget(prompt,1);
					else 
						widget = new NumericQuestionWidget(prompt,"");	
				break;
			}
			break;
			case Constants.CONTROL_SELECT_ONE:
			//go to SelectQuestion widget
			if (fromFormView == true)
				widget = new Select1QuestionWidget(prompt,'c');
			else	
			if (direction == true)
				widget = new Select1QuestionWidget(prompt,1);
			else 
				widget = new Select1QuestionWidget(prompt,"");				
			break;
		case Constants.CONTROL_SELECT_MULTI:
			//go to SelectQuestion Widget
			if (fromFormView == true)
				widget = new SelectQuestionWidget(prompt,'c');
			else	
			if (direction == true)
				widget = new SelectQuestionWidget(prompt,1);
			else 
				widget = new SelectQuestionWidget(prompt,"");				
			break;
		case Constants.CONTROL_TEXTAREA:
			//go to TextQuestion Widget
			if (fromFormView == true)
				widget = new TextQuestionWidget(prompt,'c');
			else	
			if (direction == true)
				widget = new TextQuestionWidget(prompt,1);
			else 
				widget = new TextQuestionWidget(prompt,"");	
			break;
		default:
			System.out.println("Unsupported type!");
			break;
		}
		widget.setCommandListener(this);
		widget.setItemCommandListner(this);
		controller.setView(widget);
	}


	public void destroy() {
		model.unregisterObservable(this);
	}


	public void setContext(FormEntryContext context) {
		// TODO Auto-generated method stub

	}


	public void show() {
		if (this.showFormView)
			showFormViewScreen();
		else
			getView(getIndex(),this.showFormView);//refresh view
	}

	private void showFormViewScreen() {
		model.setQuestionIndex(FormIndex.createBeginningOfFormIndex());
		formView = new FormViewScreen(this.model);
		formView.setCommandListener(this);
		controller.setView(formView);
	}

	public void refreshView()
	{
		getView(getIndex(),this.showFormView);//refresh view
	}

	public void formComplete() {

	  try {
		   Thread.sleep(1000);
		  } catch (InterruptedException ie) { }

		controller.save();//always save form
		controller.exit();

	}


	public void questionIndexChanged(FormIndex questionIndex) {
		if (questionIndex.isInForm())
			getView(getIndex(),this.showFormView);//refresh view
	}


	public void saveStateChanged(int instanceID, boolean dirty) {
		// TODO Auto-generated method stub

	}



	public void commandAction(Command command, Displayable arg1)
	{
		if(arg1 == formView){
			if (command == formView.backCommand) {
				this.show();
			} else if (command == formView.exitNoSaveCommand) {
				controller.exit();
			} else if (command == formView.exitSaveCommand) {
				controller.save();
				controller.exit();
			} else if (command == formView.sendCommand) {
				//check if all required questions are complete
				int counter = 0;
				
				for (FormIndex a = model.getForm().incrementIndex(FormIndex.createBeginningOfFormIndex());
					 a.compareTo(FormIndex.createEndOfFormIndex()) < 0;
					 a = model.getForm().incrementIndex(a)) {
					FormElementBinding bind = new FormElementBinding(null, a, model.getForm());
					
					if (bind.instanceNode.required && bind.getValue() == null)
					{
						//set counter for incomplete questions
						counter++;
					}
					
				}
				if(counter >0)
				{
					//show alert
					String txt = "There are "+counter+" unanswered compulsory questions and must be completed first to proceed";
					final Alert alert = new Alert("Question Required!", txt, null, AlertType.ERROR);
					controller.setView(new IView() {public Object getScreenObject() { return alert;}});
				}
				else
				model.setFormComplete();
				//controller.exit();
			} else if (command == List.SELECT_COMMAND) {
				int i = formView.getSelectedIndex();
				FormIndex b = formView.indexHash.get(i);
				controller.selectQuestion(b);
				
				this.showFormView = false;
			}

		}else{
			if (command == SingleQuestionScreen.nextItemCommand || command == SingleQuestionScreen.nextCommand) {
				answer=widget.getWidgetValue();

				if	(prompt.instanceNode.required && answer == null)
				{
					String txt = "This is a compulsory question and must be completed first to proceed";
					//#style CL_Forms_Form
					final Alert alert = new Alert("Question Required!", txt, null, AlertType.ERROR);
					controller.setView(new IView() {public Object getScreenObject() {return alert;}});
				}
				else{
				//save and proceed to next question
					controller.commitAnswer(this.prompt, answer);
					if(model.getForm().incrementIndex(model.getQuestionIndex()).isInForm())
					{						
						direction = true;
						controller.stepQuestion(direction);
					}
					else{
					   controller.save();//always save
					   this.showFormView = true;
					   showFormViewScreen();
					}
				}
			}
			else if (command == SingleQuestionScreen.previousCommand) {
				direction = false;
				controller.stepQuestion(direction);
				
			}
			else if (command == SingleQuestionScreen.viewAnswersCommand){
				controller.selectQuestion(FormIndex.createBeginningOfFormIndex());
				this.showFormView = true;				
				showFormViewScreen();
			}
		}
	}

    public void commandAction(Command c, Item item)
    {
    	if(c == SingleQuestionScreen.nextItemCommand)
      {
			answer=widget.getWidgetValue();
			controller.questionAnswered(this.prompt, answer);//store answers
			refreshView();

      }
    }

	public boolean isShowOverView() {
		return showFormView;
	}

	public void setShowOverView(boolean showOverView) {
		this.showFormView = showOverView;
	}
	
	public Object getScreenObject() {
		return parent.getScreenObject();
	}

	public void startOfForm() {
		// TODO Auto-generated method stub
		
	}
}