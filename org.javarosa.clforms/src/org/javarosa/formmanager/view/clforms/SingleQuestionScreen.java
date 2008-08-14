package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;

import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.IFormEntryView;

//import de.enough.polish.util.Locale;

public class SingleQuestionScreen implements IFormEntryView, FormEntryModelListener, CommandListener {

	private FormEntryController controller;
	private FormEntryModel model;

	private boolean multiLingual;

	private List screen;

	// GUI elements
	private Command backCommand;
	private Command exitNoSaveCommand;
	private Command exitSaveCommand;
	private Command saveCommand;
	private Command languageSubMenu;
	private Command[] languageCommands;
	private Gauge progressBar;
	private Command saveAndReloadCommand;

	public SingleQuestionScreen() {

	}

	public SingleQuestionScreen (String formTitle, FormEntryModel model, FormEntryController controller) {
        //#style framedForm
//    	super(formTitle);
		screen = new List(formTitle,List.IMPLICIT);

    	this.model = model;
    	this.controller = controller;
    	controller.setView(this);

//    	multiLingual = (model.getForm().getLocalizer() != null);
    	model.registerObservable(this);

	}

	public void commandAction(Command command, Displayable s) {

		if (command == backCommand) {
			System.out.println("back");
			controller.stepQuestion(false);
		} else if (command == exitNoSaveCommand) {
			controller.exit();
		} else if (command == exitSaveCommand) {
			controller.save();
			controller.exit();
		} else if (command == saveCommand) {
			controller.save();
		} else if (command == List.SELECT_COMMAND) {
			controller.selectQuestion(((List) screen).getSelectedIndex());
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
		backCommand = new Command("Back", Command.BACK, 2);
		exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
		exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
		saveCommand = new Command("Save", Command.SCREEN, 4);
		saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);

		// next command is added on a per-widget basis
		screen.addCommand(backCommand);
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

	protected void createView() {

		//NeedModel

		//Check who's relevant and display
//		form.calculateRelevantAll();

		setUpCommands();
		for (int i = 0; i < 3; i++) {
			((List) screen).append("SomeText",null);
			// Long text + value on new line indent
			// ((List)screen
			// ).append(((Prompt)form.getPrompts().elementAt(i)).getLongText()+"\n
			// A:"+temp,null);
			}


	}

	public void show() {
		createView();
		controller.setDisplay(screen);
	}

	public void destroy() {
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
