package org.javarosa.clforms.view;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.MVCComponent;
import org.javarosa.clforms.api.Prompt;
import org.javarosa.clforms.api.ResponseEvent;
import org.javarosa.clforms.util.J2MEUtil;

import de.enough.polish.util.TextUtil;

//import de.enough.polish.util.Locale;


public class PromptScreen extends MVCComponent implements IPrompter, ItemCommandListener {

	private Prompt p;

	private Controller controller;

	private static Displayable screen = null;

	private static Command previousCommand;

	/*private static Command saveAndReloadCommand;

	private static Command nextCommand;

	private static Command cancelCommand;*/
	
	private static Command goToListCommand;
	
	private Command backItemCommand = new Command("back Item Command", Command.ITEM, 1);
	private Command nextItemCommand = new Command("next Item Command", Command.ITEM, 1);

	private static TextField commentField;

	private static DateField datePicker;

	private static ChoiceGroup selectChoice;

	private int screenIndex;
	private int totalScreens;

	private String trueString = "true"; //$NON-NLS-1$
	private String falseString = "false"; //$NON-NLS-1$


	public PromptScreen() {
		System.out.println("prompt screen init");
	}

	public PromptScreen(Prompt p) {
		this.p = p;
	}

	public PromptScreen(Prompt p, int screenIndex, int totalScreens) {
		this.p = p;
		this.screenIndex = screenIndex;
		this.totalScreens = totalScreens;
	}
	
	public void showError(String title, String message, Display display)
	{
		throw new RuntimeException("Method not supported yet.");
	}

	public Displayable getScreen() {
		return screen;
	}
	
	public void save() {

		switch (p.getReturnType()){
		case org.javarosa.clforms.api.Constants.RETURN_INTEGER:
			if (commentField.getString().length() != 0){				
				p.setValue(new Integer(Integer.parseInt(commentField.getString())));				
			}
			else {
				p.setValue(null);
			}
			break;
		case org.javarosa.clforms.api.Constants.RETURN_STRING:
			p.setValue(commentField.getString());
			break;
		case org.javarosa.clforms.api.Constants.RETURN_DATE:
			if (datePicker.getDate() != null){				
				p.setValue(datePicker.getDate());
			}
			else {
				p.setValue(null);
			}
			break;
		case org.javarosa.clforms.api.Constants.RETURN_SELECT1:
			String choice = selectChoice.getString(selectChoice
					.getSelectedIndex());
			p.setSelectedIndex(selectChoice.getSelectedIndex());
			p.setValue(p.getSelectMap().get(choice));
			break;
		case org.javarosa.clforms.api.Constants.RETURN_SELECT_MULTI:

			// Find the selected values and add them to a vector
			boolean selected[] = new boolean[selectChoice.size()];
			// Fill array indicating whether each element is checked
			selectChoice.getSelectedFlags(selected);

			Vector chosenValues = new Vector();

			for (int i = 0; i < selectChoice.size(); i++){
				if (selected[i])
					chosenValues.addElement(p.getSelectMap().get(selectChoice.getString(i)));
			}
			p.setValue(chosenValues);
			break;
		case org.javarosa.clforms.api.Constants.RETURN_BOOLEAN:

			String result = selectChoice.getString(selectChoice
					.getSelectedIndex());

			p.setSelectedIndex(selectChoice.getSelectedIndex());
			if (result.equals(this.trueString)){
				p.setValue(new Boolean(true));				
			}else
				p.setValue(new Boolean(false));

			break;
		default:
			break;
		}
	}

	// TODO should this be in the controller?
	/*
	 * Method checks if element is required, i.e. cannot be empty
	 * returns false if element is not required or is not empty
	 * returns true if element is required AND is empty
	 */
	public boolean checkRequired() {
		boolean result = false;
		//LOG
		// System.out.println("Checking required for: "+p.getLongText()+" req? "+p.isRequired());
		//only if the Prompt is required we perform the check
		//TODO  change this case statement from returntype to widget type
		if (p.isRequired()){

			switch (p.getReturnType()) {
			case org.javarosa.clforms.api.Constants.RETURN_INTEGER:
				if (commentField.getString().length() == 0){				
					result = true;
				}
				else {
					result = false;
				}
				break;
			case org.javarosa.clforms.api.Constants.RETURN_STRING:
				if (commentField.getString().length() == 0){				
					result = true;
				}
				else {
					result = false;
				}
				break;
			case org.javarosa.clforms.api.Constants.RETURN_DATE:			
				if (datePicker.getDate() == null){				
					result = true;
				}
				else {
					result = false;
				}
				break;
			case org.javarosa.clforms.api.Constants.RETURN_SELECT1:
				// TODO - this is always has a default selected -how do we handle this?
				result = false;

				break;
			case org.javarosa.clforms.api.Constants.RETURN_SELECT_MULTI:

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
			case org.javarosa.clforms.api.Constants.RETURN_BOOLEAN:
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
		alert.setString("This question is required, please complete it before continuing"); //$NON-NLS-1$

		display.setCurrent(alert);

	}
	
    public void commandAction(Command c, Item item)
    {
    	if(c == nextItemCommand)
      {
      	save();
		if (!checkRequired()){
			controller.processEvent(new ResponseEvent(ResponseEvent.NEXT, -1));
		}
		else
			requiredAlert();	
      	
      }else if (c == backItemCommand) {
			save();
			controller.processEvent(new ResponseEvent(ResponseEvent.PREVIOUS, -1));
		} 
    	
    	
    }

	public void commandAction(Command command, Displayable s) {
		try {
			/*if (command == nextCommand) {
				save();
				controller.processEvent(new ResponseEvent(ResponseEvent.NEXT, -1));
				if (!checkRequired()){
				}
				else
					requiredAlert();							
			}*/
			if (command == previousCommand) {
				save();
				controller.processEvent(new ResponseEvent(ResponseEvent.PREVIOUS, -1));
			}
			/* 
			else if (command == saveAndReloadCommand) {
				save();
				controller.processEvent(new ResponseEvent(ResponseEvent.SAVE_AND_RELOAD, -1));
			} 
			else if (command == cancelCommand){
				controller.processEvent(new ResponseEvent(ResponseEvent.LIST,-1));
			}
			else */
				if (command == goToListCommand){
				save();
				controller.processEvent(new ResponseEvent(ResponseEvent.LIST,-1));
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

	protected void createView() throws Exception {
		previousCommand = new Command("back", Command.SCREEN, 2); //$NON-NLS-1$
		/*nextCommand = new Command("next->", Command.BACK, 1); //$NON-NLS-1$
		cancelCommand = new Command("Cancel to list", Command.ITEM, 4);
		saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 5);*/
		goToListCommand = new Command("Form View", Command.SCREEN, 1);

		// screen by type
		switch (p.getReturnType()) {
		case org.javarosa.clforms.api.Constants.RETURN_INTEGER:
			intForm();
			break;
		case org.javarosa.clforms.api.Constants.RETURN_STRING:
			stringForm();
			break;
		case org.javarosa.clforms.api.Constants.RETURN_DATE:
			dateForm();
			break;
		case org.javarosa.clforms.api.Constants.RETURN_SELECT1:
			select1Form();
			break;
		case org.javarosa.clforms.api.Constants.RETURN_SELECT_MULTI:
			selectForm();
			break;
		case org.javarosa.clforms.api.Constants.RETURN_BOOLEAN:
			booleanForm();
			break;
		}

		setHint();

		/*screen.addCommand(nextCommand);
		screen.addCommand(saveAndReloadCommand);
		screen.addCommand(cancelCommand);*/
		screen.addCommand(previousCommand);
		screen.addCommand(goToListCommand);
	}




	public void setHint(){
		if (p.getHint() != null){
		
			Ticker t = new Ticker(p.getHint());
			((Form) screen).setTicker(t); //$NON-NLS-1$
		}
	}


	private void intForm() {

		commentField = new TextField(p.getLongText(), "", 40, TextField.NUMERIC); //$NON-NLS-1$
		String[] parameters = new String[2];
		parameters[0] = new Integer(screenIndex+1).toString();
		parameters[1] = new Integer(this.totalScreens).toString();
		screen = new Form(screenIndex+"/"+totalScreens);
			
		//check if the field has already been filled in - if so display value
		if (p.getValue() != null){
			System.out.println("VALUE NOT NULL");
			commentField.setString(((Integer)p.getValue()).toString());
		}
		else if (p.getDefaultValue() != null){
			System.out.println("DEFVALUE NOT NULL");
			commentField.setString(((Integer)p.getDefaultValue()).toString());
		}

		((Form) screen).append(commentField);

		addNavigaitonButtons();
	}

	private void stringForm() {

		commentField = new TextField(p.getLongText(), "", 40, TextField.ANY); //$NON-NLS-1$
		String[] parameters = new String[2];
		parameters[0] = new Integer(screenIndex+1).toString();
		parameters[1] = new Integer(this.totalScreens).toString();
		screen = new Form(screenIndex+"/"+totalScreens);

		//check if the field has already been filled in - if so display value
		if (p.getValue() != null){
			commentField.setString((String)p.getValue());
		}
		else if (p.getDefaultValue() != null){
			commentField.setString(((String)p.getDefaultValue()));
		}


		((Form) screen).append(commentField);

		addNavigaitonButtons();
	}

	private void addNavigaitonButtons() {
		StringItem backItem = new StringItem(null,"BACK",Item.BUTTON); 
		StringItem nextItem = new StringItem(null,"NEXT",Item.BUTTON);
		
		
		//nextItem.setPreferredSize(40,25);
		//backItem.setPreferredSize(40,25);
		/*nextItem.setLayout(Item.LAYOUT_LEFT);
		backItem.setLayout(Item.LAYOUT_RIGHT);*/
		
		((Form) screen).append(nextItem);
	    //((Form) screen).append(backItem);

	    backItem.setDefaultCommand(backItemCommand);     // add Command to Item.
	    backItem.setItemCommandListener(this);       // set item command listener
	    nextItem.setDefaultCommand(nextItemCommand);     // add Command to Item.
	    nextItem.setItemCommandListener(this);       // set item command listener
		
	}

	private void dateForm() {

		datePicker = new DateField(p.getLongText(), DateField.DATE);

		String[] parameters = new String[2];
		parameters[0] = new Integer(screenIndex+1).toString();
		parameters[1] = new Integer(this.totalScreens).toString();
		screen = new Form(screenIndex+"/"+totalScreens);

		//check if the field has already been filled in - if so display value
		if (p.getValue() != null){
			datePicker.setDate((Date)p.getValue());
		}
		else if (p.getDefaultValue() != null){
			datePicker.setDate((Date)p.getDefaultValue());
		}

		((Form) screen).append(datePicker);
		
		addNavigaitonButtons();

	}

	private void select1Form() {

		selectChoice = new ChoiceGroup(p.getLongText(), ChoiceGroup.EXCLUSIVE);

		Enumeration itr = p.getSelectMap().keys();
		int i =0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			selectChoice.append(label, null);
			i++;
		}

		String[] parameters = new String[2];
		parameters[0] = new Integer(screenIndex+1).toString();
		parameters[1] = new Integer(this.totalScreens).toString();
		screen = new Form(screenIndex+"/"+totalScreens);

		//check if the field has already been filled in - if so display value
		if (p.getValue() != null){
			if(p.getSelectedIndex()!= -1){
				selectChoice.setSelectedIndex(p.getSelectedIndex(),true);
			}else{
				selectChoice = (ChoiceGroup)J2MEUtil.setSelected(p,selectChoice);
			}
		}

		// ((Form) screen).append(p.getHintText());
		((Form) screen).append(selectChoice);
		
		addNavigaitonButtons();

	}

	private void selectForm() {
		// get set of question options
		selectChoice = new ChoiceGroup(p.getLongText(), ChoiceGroup.MULTIPLE);
		Enumeration itr = p.getSelectMap().keys();
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			selectChoice.append(label, null);
			i++;
		}
		
		// if currently has a vector assign selected indexes
		i = 0;
		Vector v = null;
		if(p.getValue() != null){
			try {
				v = (Vector) p.getValue();
				itr = p.getSelectMap().keys();
				while (itr.hasMoreElements()) {
					String label = (String) itr.nextElement();
					try{
						if(v.contains(p.getSelectMap().get(label)))
							selectChoice.setSelectedIndex(i,true);					
					}
					catch(ClassCastException e){
						System.out.println(e.getMessage()); //$NON-NLS-1$
						e.printStackTrace();
					}
					i++;
				}
			} catch (ClassCastException e) {
				System.out.println("IN MULTI decompose: "+(String)p.getValue().toString()+p.getValue().getClass());
				selectChoice = (ChoiceGroup)J2MEUtil.setSelected(p,selectChoice);
			}
		}else{
			System.out.println("V null..");
		}

		String[] parameters = new String[2];
		parameters[0] = new Integer(screenIndex+1).toString();
		parameters[1] = new Integer(this.totalScreens).toString();
		screen = new Form(screenIndex+"/"+totalScreens);
		// ((Form) screen).append(e.getHintText());
		((Form) screen).append(selectChoice);
		
		addNavigaitonButtons();

	}

	private void booleanForm() {

		selectChoice = new ChoiceGroup(p.getLongText(), ChoiceGroup.EXCLUSIVE);

		selectChoice.append(this.trueString, null);
		selectChoice.append(this.falseString, null);

		String[] parameters = new String[2];
		parameters[0] = new Integer(screenIndex+1).toString();
		parameters[1] = new Integer(this.totalScreens).toString();
		screen = new Form(screenIndex+"/"+totalScreens);

		//check if the field has already been filled in - if so display value
		if (p.getValue() != null){
			
			if(p.getSelectedIndex() != -1){
				selectChoice.setSelectedIndex(p.getSelectedIndex(),true);
			} else{
				for (int i=0; i<selectChoice.size();i++){
					if(TextUtil.equalsIgnoreCase(selectChoice.getString(i),p.getValue().toString())){
						selectChoice.setSelectedIndex(i, true);
					}
				}
			}
		}

		((Form) screen).append(selectChoice);
		
		addNavigaitonButtons();

	}

	protected void updateView() throws Exception {
		createView();
	}

	public void registerController(Controller controller) {
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
	
	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
		this.p = prompt;
		this.screenIndex = screenIndex;
		this.totalScreens = totalScreens;
		try{
			createView();
			showScreen();
		}catch(Exception e){
			System.out.println("something wrong in CREAT VIEW\n "+e.getMessage());
			e.printStackTrace();			
		}

	}

}
