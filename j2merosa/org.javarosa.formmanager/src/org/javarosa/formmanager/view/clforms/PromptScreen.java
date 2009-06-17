/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.controller.FormEntryController;


public class PromptScreen extends Form implements ItemCommandListener, CommandListener {


	private FormEntryController controller;
	private QuestionDef prompt;

	private static Command previousCommand;
	private static Command nextCommand;
	private static Command goToListCommand;

	private Command backItemCommand = new Command("back Item Command", Command.ITEM, 1);
	private Command nextItemCommand = new Command("next Item Command", Command.ITEM, 1);

	private static TextField commentField;

//	private static DateField datePicker;

//	private static ChoiceGroup selectChoice;

	private FormViewScreen parent;

	public PromptScreen(String title) {
		super(title);
		System.out.println("prompt screen init");
	}

	public PromptScreen(QuestionDef prompt) {
		super(prompt.getName());
		this.prompt = prompt;
	}


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
		return false;
	}


	public void requiredAlert(){

		Alert alert = new Alert("alert"); //$NON-NLS-1$
		alert.setType(AlertType.WARNING);

		alert.setTimeout(Alert.FOREVER);
		alert.setString("This question is required, please complete it before continuing"); //$NON-NLS-1$

//		display.setCurrent(alert);

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
			requiredAlert();

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
					requiredAlert();
			}
			else if (command == previousCommand) {
				parent.show();
			}
			else if (command == goToListCommand){
				 parent.show();
			}

	}


	protected void createView(){
		addCommands();

		setHint();
		stringForm();
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



		screen.addCommand(saveAndReloadCommand);
		screen.addCommand(cancelCommand);
		screen.addCommand(nextCommand);
		screen.addCommand(goToListCommand);
	}

	private void addCommands() {
		previousCommand = new Command("back", Command.SCREEN, 2); //$NON-NLS-1$
		nextCommand = new Command("next", Command.SCREEN, 1); //$NON-NLS-1$
		goToListCommand = new Command("View Answers", Command.SCREEN, 1);

		this.addCommand(previousCommand);
		this.addCommand(nextCommand);
		this.addCommand(goToListCommand);
		this.setCommandListener(this);
	}




	public FormEntryController getController() {
		return controller;
	}

	public void setController(FormEntryController controller) {
		this.controller = controller;
	}

	public void setHint(){
		if (prompt.getHelpText()!= null){

			Ticker t = new Ticker(prompt.getHelpText());
			this.setTicker(t);
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

	private void addNavigaitonButtons() {
		StringItem backItem = new StringItem(null,"BACK",Item.BUTTON);
		StringItem nextItem = new StringItem(null,"NEXT",Item.BUTTON);


		//nextItem.setPreferredSize(40,25);
		//backItem.setPreferredSize(40,25);
		nextItem.setLayout(Item.LAYOUT_LEFT);
		backItem.setLayout(Item.LAYOUT_RIGHT);

		this.append(nextItem);
	    //((Form) screen).append(backItem);

	    backItem.setDefaultCommand(backItemCommand);     // add Command to Item.
	    backItem.setItemCommandListener(this);       // set item command listener
	    nextItem.setDefaultCommand(nextItemCommand);     // add Command to Item.
	    nextItem.setItemCommandListener(this);       // set item command listener

	}

	private void stringForm() {

		this.setTitle(prompt.getName());
		commentField = new TextField(prompt.getLongText(), "", 40, TextField.ANY);
		this.append(commentField);

		//check if the field has already been filled in - if so display value
		if (p.getValue() != null){
			commentField.setString((String)p.getValue());
		}
		else if (p.getDefaultValue() != null){
			commentField.setString(((String)p.getDefaultValue()));
		}


		addNavigaitonButtons();
	}

	private void dateForm() {

		DateField datePicker = new DateField(p.getLongText(), DateField.DATE);

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

	public void show() {
		createView();
		controller.setDisplay(this);
	}


	public void setParent(FormViewScreen formViewScreen) {
		this.parent = formViewScreen;

	}

}
*/