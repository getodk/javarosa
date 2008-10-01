package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.utility.SortedIntSet;
import org.javarosa.formmanager.view.IFormEntryView;

public class FormViewScreen extends List {

	private FormEntryModel model;
	private FormViewManager parent;

	public SortedIntSet indexHash;

	// GUI elements
	public static Command exitNoSaveCommand;
	public static Command exitSaveCommand;
	public static Command sendCommand;
	public static Command backCommand;

	public FormViewScreen (FormEntryModel model) {
        //#style CL_Forms_Form
    	super(model.getForm().getName(),List.IMPLICIT);
    	this.model = model;
    	createView();
		setUpCommands();
	}

	private void setUpCommands() {
		exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
		exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
		sendCommand = new Command("Send Form", Command.SCREEN, 4);
		//saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);

		// next command is added on a per-widget basis
		this.addCommand(exitNoSaveCommand);
		//screen.addCommand(exitSaveCommand);
		this.addCommand(sendCommand);
		//screen.addCommand(saveAndReloadCommand);
	}

	protected void createView() {

		//Check who's relevant and display
//		form.calculateRelevantAll();

		//first ensure clean gui
		((List) this).deleteAll();
		indexHash = new SortedIntSet();

		for (int i = 0; i < model.getNumQuestions(); i++) {
			// Check if relevant
			if(model.isRelevant(i))
			{
				String stringVal;
				// Get current value as STring
				IAnswerData  val = model.getForm().getValue(model.getQuestion(i));
				//check for null answers
				if(val == null){
					stringVal = "";
				}
				else {
				stringVal = val.getDisplayText();
				}

				// Append to list
				((List) this).append(model.getQuestion(i).getShortText()+" => "+stringVal,null);

				indexHash.add(i);//map list index to question index.
			}
		}
	}

}
