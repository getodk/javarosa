package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;

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

	private boolean multiLingual;

	private List screen;

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

    	multiLingual = (model.getForm().getLocalizer() != null);
    	model.registerObservable(this);
    	
		setUpCommands();

	}

	public void commandAction(Command command, Displayable s) {
		if(s instanceof FormViewManager){
//			if (command == P)

			controller.stepQuestion(true);
		}

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
			controller.selectQuestion(i);
			backCommand = new Command("Back", Command.BACK, 2);
//form view manager starts here...
			FormViewManager manager = new FormViewManager("Questions",model,controller,i,this);
			manager.show();

		} else {
			String language = null;
			if (multiLingual) {
				for (int i = 0; i < languageCommands.length; i++) {
					if (command == languageCommands[i]) {
						language = command.getLabel();
						break;
					}
				}
			}

			if (language != null) {
				controller.setLanguage(language);
			} else {
				System.err
						.println("Chatterbox: Unknown command event received ["
								+ command.getLabel() + "]");
			}
		}
	}

	private void setUpCommands() {
		exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
		exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
		saveCommand = new Command("Save", Command.SCREEN, 4);
		saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);

		if (multiLingual) {
			languageSubMenu = new Command("Language", Command.SCREEN, 2);
			populateLanguages();
		}

		// next command is added on a per-widget basis
		screen.addCommand(exitNoSaveCommand);
		screen.addCommand(exitSaveCommand);
		screen.addCommand(saveCommand);
		screen.addCommand(saveAndReloadCommand);

		if (languageSubMenu != null) {
			screen.addCommand(languageSubMenu);
			// for (int i = 0; i < languageCommands.length; i++)
			// screen.addSubCommand(languageCommands[i], languageSubMenu);
			// Whats a subCommand??
		}

		screen.setCommandListener(this);
	}

	private void populateLanguages() {
		String[] availableLocales = model.getForm().getLocalizer()
				.getAvailableLocales();
		languageCommands = new Command[availableLocales.length];
		for (int i = 0; i < languageCommands.length; i++)
			languageCommands[i] = new Command(availableLocales[i],
					Command.SCREEN, 3);
	}

	protected void createView() {

		//Check who's relevant and display
//		form.calculateRelevantAll();

		//first ensure clean gui
		((List) screen).deleteAll();
		
		for (int i = 0; i < model.getNumQuestions(); i++) {
			// Check if relevant
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
