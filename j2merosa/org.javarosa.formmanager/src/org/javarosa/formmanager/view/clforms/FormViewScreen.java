package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.javarosa.core.api.IView;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.SortedIndexSet;
import org.javarosa.formmanager.view.FormElementBinding;
//#if javarosa.usepolishlocalisation
import de.enough.polish.util.Locale;
//#endif

public class FormViewScreen extends List implements IView {

	private FormEntryModel model;
	private FormViewManager parent;

	public SortedIndexSet indexHash;

	// GUI elements
	public static Command exitNoSaveCommand;
	public static Command exitSaveCommand;
	public static Command sendCommand;
	public static Command backCommand;

	public FormViewScreen (FormEntryModel model) {
        //#style CL_Forms_Form
    	super(model.getForm().getTitle(),List.IMPLICIT);
    	this.model = model;
    	createView();
		setUpCommands();
	}

	private void setUpCommands() {
		//#if javarosa.usepolishlocalisation
		exitNoSaveCommand = new Command(Locale.get("menu.Exit"), Command.EXIT, 4);
		exitSaveCommand = new Command(Locale.get("menu.SaveAndExit"), Command.SCREEN, 4);
		sendCommand = new Command(Locale.get("menu.SendForm"), Command.SCREEN, 4);
		//#else
		exitNoSaveCommand = new Command("Exit", Command.EXIT, 4);
		exitSaveCommand = new Command("Save and Exit", Command.SCREEN, 4);
		sendCommand = new Command("Send Form", Command.SCREEN, 4);
		//#endif
		//saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);

		// next command is added on a per-widget basis
		this.addCommand(exitNoSaveCommand);
		//screen.addCommand(exitSaveCommand);
		if(!model.isReadOnly()){
		this.addCommand(sendCommand);
		}
		//screen.addCommand(saveAndReloadCommand);
	}

	protected void createView() {

		//Check who's relevant and display
//		form.calculateRelevantAll();

		//first ensure clean gui
		((List) this).deleteAll();
		indexHash = new SortedIndexSet();

		for (FormIndex i = model.getForm().incrementIndex(FormIndex.createBeginningOfFormIndex());
			 i.compareTo(FormIndex.createEndOfFormIndex()) < 0;
			 i = model.getForm().incrementIndex(i)) {
			// Check if relevant
			if(model.isRelevant(i))
			{
				FormElementBinding bind = new FormElementBinding(null, i, model.getForm());
				
				String stringVal;
				// Get current value as STring
				IAnswerData  val = bind.getValue();
				//check for null answers
				if(val == null){
					stringVal = "";
				}
				else {
				stringVal = val.getDisplayText();
				}

				if(bind.instanceNode.required){
				// Append to list
				((List) this).append("*"+((QuestionDef)bind.element).getShortText()+" => "+stringVal,null);
				}
				else
				{
					((List) this).append(((QuestionDef)bind.element).getShortText()+" => "+stringVal,null);
				}
				indexHash.add(i);//map list index to question index.
			}
		}
	}
	public Object getScreenObject() {
		return this;
	}
}
