package org.javarosa.formmanager.view.clforms;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.IFormEntryView;

//import de.enough.polish.util.Locale;

public class FormViewScreen implements IFormEntryView, FormEntryModelListener, CommandListener {

	private FormEntryController controller;
	private FormEntryModel model;

	private List screen;
	private Vector indexHash;

	// GUI elements
	private Command exitNoSaveCommand;
	private Command exitSaveCommand;
	private Command saveCommand;
	private Command languageSubMenu;
	private Command[] languageCommands;
	private Gauge progressBar;
	private Command saveAndReloadCommand;
	private Command backCommand;

	private Form tempForm;
	private PromptScreen questionScreen;

	public FormViewScreen() {

	}

	public FormViewScreen (String formTitle, FormEntryModel model, FormEntryController controller) {
        //#style framedForm
//    	super(formTitle);
//		screen = new List(formTitle,List.IMPLICIT);
		screen = new List(model.getForm().getName(),List.IMPLICIT);

    	this.model = model;
    	this.controller = controller;
    	controller.setView(this);

    	model.registerObservable(this);

		setUpCommands();

	}

	public void commandAction(Command command, Displayable s) {
		/*if(s instanceof FormViewManager){
//			if (command == P)

			controller.stepQuestion(true);
		}*/

		if (command == backCommand) {
			this.show();
		} else if (command == exitNoSaveCommand) {
			controller.exit();
		} else if (command == exitSaveCommand) {
			controller.save();
			controller.exit();
		} else if (command == saveCommand) {
			controller.save();
		} else if (command == List.SELECT_COMMAND) {
			int i = ((List) screen).getSelectedIndex();	
			//System.out.println("list chosen"+ i);
			QuestionDef a = (QuestionDef)indexHash.elementAt(i);//get question corresponding to list index
			controller.selectQuestion(a.getID()-1);	
			//System.out.println("controller sets"+a.getID());
			int b = model.getQuestionIndex();
			//System.out.println("viewmanager sets"+b);
			backCommand = new Command("Back", Command.BACK, 2);
//form view manager starts here...
			FormViewManager manager = new FormViewManager("Questions",model,controller,b,this);
			manager.show();

		}
	}

	private void setUpCommands() {
		exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
		exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
		saveCommand = new Command("Save", Command.SCREEN, 4);
		saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);

		// next command is added on a per-widget basis
		screen.addCommand(exitNoSaveCommand);
		screen.addCommand(exitSaveCommand);
		screen.addCommand(saveCommand);
		screen.addCommand(saveAndReloadCommand);
		screen.setCommandListener(this);
	}

	protected void createView() {

		//Check who's relevant and display
//		form.calculateRelevantAll();

		//first ensure clean gui
		((List) screen).deleteAll();
		indexHash = new Vector();

		for (int i = 0; i < model.getNumQuestions(); i++) {
			
			// Check if relevant
			if(model.isRelevant(i))
			{

				String stringVal;
				// Get current value as STring
				IAnswerData  val = model.getForm().getValue(model.getQuestion(i));
				//check for null answers
				if(val == null)
				{System.out.println("no answer stored for question");
					stringVal = null;
				}
				else
				{
				stringVal = val.getDisplayText();
				}

				if (stringVal == null){
					stringVal = new String("Unanswered");
				}

				// Append to list
				((List) screen).append(model.getQuestion(i).getShortText()+"   =>   "+stringVal,null);
				
				indexHash.addElement(model.getQuestion(i));//map list index to question index.
	
			}

		}



	}

	public void show() {
		createView();
		controller.setDisplay(screen);

	}

	public void destroy() {
		model.unregisterObservable(this);
	}

	public void formComplete() {
//		progressBar.setValue(model.getNumQuestions());
//		repaint();
		try {
			Thread.sleep(1000); //let them bask in their completeness
		} catch (InterruptedException ie) { }

		controller.save();
		controller.exit();
	}

	public void questionIndexChanged(int questionIndex) {

	}

	public void saveStateChanged(int instanceID, boolean dirty) {

	}

	public void setContext(FormEntryContext context) {
		// TODO Auto-generated method stub

	}

}
