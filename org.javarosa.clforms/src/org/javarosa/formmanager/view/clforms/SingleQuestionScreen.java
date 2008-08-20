package org.javarosa.formmanager.view.clforms;

//import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.clforms.widgets.DateQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.Select1QuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.SelectQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TextQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TimeQuestionWidget;
import org.javarosa.core.model.Constants;

import de.enough.polish.ui.Alert;


public class SingleQuestionScreen extends Form implements IFormEntryView, FormEntryModelListener, CommandListener, ItemCommandListener {

	private FormEntryController controller;
	private FormEntryModel model;
	private FormViewScreen parent;

	//private Alert alert;
	private boolean multiLingual;
	private QuestionDef prompt;
	
	//commands
	private static Command previousCommand;
	private static Command nextCommand;
	private static Command goToListCommand;
	private Command backItemCommand = new Command("back Item Command", Command.ITEM, 1);
	private Command nextItemCommand = new Command("next Item Command", Command.ITEM, 1);

		
	// GUI elements
	private Gauge progressBar;


	public SingleQuestionScreen(QuestionDef prompt) {
		super(prompt.getName());
		this.prompt = prompt;
	}
	public SingleQuestionScreen (String formTitle) {
        //#style framedForm
    	super(formTitle);
	}
	
	public FormEntryController getController() {
		return controller;
	}

	public void setController(FormEntryController controller) {
		this.controller = controller;
	}
	
    public void commandAction(Command c, Item item)
    {
    	if(c == nextItemCommand)
      {
		if (!checkRequired()){
//			controller.questionAnswered(this.prompt, null);
			controller.stepQuestion(true);
		}
		else
			showError("Stop!", "This is a required question, you must complete it");

      }else if (c == backItemCommand) {
			controller.stepQuestion(false);
		}


    }
	public void commandAction(Command command, Displayable s) {
			if (command == nextCommand) {
				parent.commandAction(nextCommand, this);

				if (!checkRequired()){
				}
				else
					showError("Stop!", "This is a required question, you must complete it");
			}
			else if (command == previousCommand) {
				parent.show();
			}
			else if (command == goToListCommand){
				 parent.show();
			}

	}

	private void setUpCommands() {System.out.println("setting up comands");
	previousCommand = new Command("back", Command.SCREEN, 2); //$NON-NLS-1$
	nextCommand = new Command("next", Command.SCREEN, 1); //$NON-NLS-1$
	goToListCommand = new Command("View Answers", Command.SCREEN, 1);

	this.addCommand(previousCommand);
	this.addCommand(nextCommand);
	this.addCommand(goToListCommand);


/*		if (languageSubMenu != null) {
			this.addCommand(languageSubMenu);
			// for (int i = 0; i < languageCommands.length; i++)
			// screen.addSubCommand(languageCommands[i], languageSubMenu);
			// Whats a subCommand??
		}*/

		this.setCommandListener(this);System.out.println("command listener set");
}
	private void addNavigationButtons() 
	{
		StringItem backItem = new StringItem(null,"BACK",Item.BUTTON);
		StringItem nextItem = new StringItem(null,"NEXT",Item.BUTTON);

		this.append(nextItem);
	    //((Form) screen).append(backItem);

	    backItem.setDefaultCommand(backItemCommand);     // add Command to Item.
	    backItem.setItemCommandListener(this);       // set item command listener
	    nextItem.setDefaultCommand(nextItemCommand);     // add Command to Item.
	    nextItem.setItemCommandListener(this);       // set item command listener

	}
	
    private void initProgressBar () {
        //#style progressbar
        progressBar = new Gauge(null, false, model.getNumQuestions(), 0);
    }
    
	protected void createView() {

		setUpCommands();
		
		//retrieve the question type from the model
		//int index = model.getQuestionIndex();
		int questionType = prompt.getDataType();
		System.out.println("IM HERE! attempting to pick a question type for" + questionType);
		/* for testing individual widgets*/
		
		//DateQuestionWidget dateQ = new DateQuestionWidget(prompt.getName());
		//this.append(dateQ.initWidget(prompt));
		TextQuestionWidget textQ = new TextQuestionWidget(prompt.getName());
		this.append(textQ.initWidget(prompt));
		/* end of testing */
		
/*		switch(questionType)//how do i know question types?
		{
		case Constants.DATATYPE_DATE:
			//go to DateQuestion Widget
			DateQuestionWidget dateQ = new DateQuestionWidget(prompt.getName());
			this.append(dateQ.initWidget(prompt));
		case Constants.DATATYPE_LIST_MULTIPLE:
			//go to SelectQuestion Widget
			SelectQuestionWidget selectQ = new SelectQuestionWidget(prompt.getName());
			this.append(selectQ.initWidget(prompt));
		case Constants.DATATYPE_LIST_EXCLUSIVE:
			//go to Select1Question Widget
			Select1QuestionWidget select1Q = new Select1QuestionWidget(prompt.getName());
		case Constants.DATATYPE_TEXT:
			//go to TextQuestion Widget
			TextQuestionWidget textQ = new TextQuestionWidget(prompt.getName());
			this.append(textQ.initWidget(prompt));
		case Constants.DATATYPE_TIME:
			//go to TimeQuestion Widget
			TimeQuestionWidget timeQ = new TimeQuestionWidget(prompt.getName());
		default:
			System.out.println("Unsupported type!");
		
		}*/
		addNavigationButtons();
	}

    
    private void commitAndSave () {
//TODO: must check if singleQuestionForm is empty first
    	controller.save();
    }

	public void show() {
		createView();
		controller.setDisplay(this);
	}

	public void destroy() {
		model.unregisterObservable(this);
	}

	public void formComplete() {
		progressBar.setValue(model.getNumQuestions());
//		repaint();
		try {
			Thread.sleep(1000); //let them bask in their completeness
		} catch (InterruptedException ie) { }

		controller.save();
		controller.exit();
	}
	public boolean checkRequired() 
	{//for now all questions are optional
		return false;
	}
	public void questionIndexChanged(int questionIndex) {

	}

	public void saveStateChanged(int instanceID, boolean dirty) {

	}

	public void setContext(FormEntryContext context) {
		// TODO Auto-generated method stub

	}
	public void setParent(FormViewScreen formViewScreen) {
		this.parent = formViewScreen;

	}
	
    private void showError(String title, String message) {
    	//#style mailAlert
    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
    	alert.setTimeout(Alert.FOREVER);
    	//alert.setCommandListener?
    	Alert.setCurrent(JavaRosaServiceProvider.instance().getDisplay(), alert, null);
    }
	

}
