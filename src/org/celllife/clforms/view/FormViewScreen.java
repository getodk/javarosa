package org.celllife.clforms.view;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import org.celllife.clforms.IController;
import org.celllife.clforms.MVCComponent;
import org.celllife.clforms.api.IForm;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.util.J2MEUtil;

//import de.enough.polish.util.Locale;


public class FormViewScreen extends MVCComponent implements FormView{

	private Prompt p;

	private IController controller;
	
	private IForm form;

	private static Displayable screen = null;

	private static Command saveAndReloadCommand;

	private static Command exitCommand;

	private static TextField commentField;

	private static DateField datePicker;

	private static ChoiceGroup selectChoice;

	public FormViewScreen() {
		System.out.println("FormView screen init");
	}

	public FormViewScreen(Prompt p) {
		this.p = p;
	}

	public Displayable getScreen() {
		return screen;
	}

	/*
	 * Method checks if element is required, i.e. cannot be empty
	 * returns false if element is not required or is not empty
	 * returns true if element is required AND is empty
	 */
	public boolean checkRequired() {
		boolean result = false;
		//only if the Prompt is required we perform the check
		if (p.isRequired()){

			switch (p.getReturnType()) {
			case org.celllife.clforms.api.Constants.RETURN_INTEGER:
				if (commentField.getString().length() == 0){				
					result = true;
				}
				else {
					result = false;
				}
				break;
			case org.celllife.clforms.api.Constants.RETURN_STRING:
				if (commentField.getString().length() == 0){				
					result = true;
				}
				else {
					result = false;
				}
				break;
			case org.celllife.clforms.api.Constants.RETURN_DATE:			
				if (datePicker.getDate() == null){				
					result = true;
				}
				else {
					result = false;
				}
				break;
			case org.celllife.clforms.api.Constants.RETURN_SELECT1:
				// TODO - this is always has a default selected -how do we handle this?
				result = false;

				break;
			case org.celllife.clforms.api.Constants.RETURN_SELECT_MULTI:

				// Find the selected values and add them to a vector
				boolean selected[] = new boolean[selectChoice.size()];
				// Fill array indicating whether each element is checked
				selectChoice.getSelectedFlags(selected);
				result = true;
				for (int i = 0; i < selectChoice.size(); i++){
					if (selected[i])
						result = false;
				}				
				break;
			case org.celllife.clforms.api.Constants.RETURN_BOOLEAN:
				// TODO - this is always has a default selected -how do we handle this?
				result = false;

				break;
			}
		}

		return result;
	}


	public void requiredAlert(){

		Alert alert = new Alert("alert"); //$NON-NLS-1$
		alert.setType(AlertType.WARNING);

		alert.setTimeout(Alert.FOREVER);
		alert.setString("error.required"); //$NON-NLS-1$

		display.setCurrent(alert);

	}

	public void commandAction(Command command, Displayable s) {
		try {
			if (command == saveAndReloadCommand) {
				controller.processEvent(new ResponseEvent(ResponseEvent.SAVE_AND_RELOAD, -1));
			}
			else if (command == List.SELECT_COMMAND){
				System.out.println("FormViewScreen.commandAction(SELECT_COMMAND) selectedIndex: " + ((List)screen).getSelectedIndex());
				controller.processEvent(new ResponseEvent(ResponseEvent.GOTO,((List)screen).getSelectedIndex()));
			}
			else if (command == exitCommand){
				controller.processEvent(new ResponseEvent(ResponseEvent.EXIT, -1));
			}
			
		} catch (Exception e) {
			Alert a = new Alert("error.screen" + " 2"); //$NON-NLS-1$
			a.setString(e.getMessage());
			a.setTimeout(Alert.FOREVER);
			display.setCurrent(a);
		}
	}

	// Initialize. If a data member is not backed by RMS, make sure
	// it is uninitilzed (null) before you put in values.
	protected void initModel() throws Exception {

	}

	protected void createView() {
		saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);
		exitCommand = new Command("Exit", Command.EXIT, 3);
		
		form = controller.getForm();
		screen = new List("FORM: "+form.getName(),List.IMPLICIT);
		form.calculateRelevantAll();
		for(int i = 0; i<form.getPrompts().size(); i++){
			
			if(((Prompt)form.getPrompt(i)).isRelevant()){
				int type = ((Prompt)form.getPrompts().elementAt(i)).getReturnType();
				String temp= J2MEUtil.getStringValue(((Prompt)form.getPrompts().elementAt(i)).getValue(),type);
				//((List)screen ).append(((Prompt)form.getPrompts().elementAt(i)).getLongText()+temp,null);
				// short text + value
				((List)screen ).append(((Prompt)form.getPrompts().elementAt(i)).getShortText()+" => " +temp,null);
				// Long text + value on new line indent
				//((List)screen ).append(((Prompt)form.getPrompts().elementAt(i)).getLongText()+"\n A:"+temp,null);
			}
			
		}
		
		screen.addCommand(saveAndReloadCommand);
		screen.addCommand(exitCommand);

	}

	protected void updateView() throws Exception {
		createView();
	}

	public void registerController(IController controller) {
		this.controller = controller;		
	}

	public void showPrompt(Prompt prompt) {
		this.p = prompt;
		try{
			createView();
			showScreen();
		}catch(Exception e){
			System.out.println("something wrong in CREAT VIEW\n "+e.getMessage());
			e.printStackTrace();			
		}

	}
	


}
