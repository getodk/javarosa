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

package org.javarosa.formmanager.view.clforms;

//import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Ticker;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.j2me.view.J2MEDisplay;



public abstract class SingleQuestionScreen extends Form implements IFormEntryView {

	protected FormEntryController controller;
	protected FormEntryModel model;

	protected FormElementBinding qDef;
	protected IAnswerData answer;

	// GUI elements
	public Gauge progressBar;

	public static Command previousCommand;
	public static Command nextCommand;
	public static Command viewAnswersCommand;
	
	public static Command nextItemCommand = new Command(Localization.get("menu.Next"), Command.ITEM, 1);
	//#style button
	public StringItem nextItem = new StringItem(null,Localization.get("button.Next"),Item.BUTTON);
	
	public ItemCommandListener itemListner;

	public SingleQuestionScreen(FormElementBinding prompt) {
		super(prompt.element.getTitle());		
		this.qDef = prompt;
		this.creatView();
		this.setUpCommands();
	}
	
	//hack: this is for moving to NEXT question
	public SingleQuestionScreen(FormElementBinding prompt, int temp) {
	//#style nextQuestion
		super(prompt.element.getTitle());
		
		this.qDef = prompt;
		this.creatView();
		this.setUpCommands();
	}

	//hack: this is for moving to PREV question
	public SingleQuestionScreen(FormElementBinding prompt, String str) {
	//#style prevQuestion
		super(prompt.element.getTitle());
		this.qDef = prompt;
		this.creatView();
		this.setUpCommands();
	}

	//hack: this is for moving to question fromViewAnswers
	public SingleQuestionScreen(FormElementBinding prompt, char str) {
	//#style fromViewAnswers
		super(prompt.element.getTitle());
		this.qDef = prompt;
		this.creatView();
		this.setUpCommands();
	}
	

/*
 * This constructor would have been the non-hacked version, but it doesn't work:
 * The first line of a constructor must be super(), not even an if statement may 
 * come before it
*/
/*public SingleQuestionScreen(QuestionDef prompt, boolean direction) {
	
		if (direction == true)
		{
			//#style nextQuestion
			super(prompt.getName());
	//		SingleQuestionScreen(prompt,1);
		}
		else
		{

			//#style prevQuestion
			super(prompt.getName());
	//		SingleQuestionScreen(prompt,"s");
		}
		this.qDef = prompt;
		this.creatView();
		this.setUpCommands();
	}*/



	public abstract void creatView();
    public abstract IAnswerData getWidgetValue ();
    
	public void setHint(String helpText) {
		javax.microedition.lcdui.Ticker tick = new javax.microedition.lcdui.Ticker("HELP: "+helpText);
		this.setTicker(tick);
	}

	private void setUpCommands(){
		nextCommand = new Command(Localization.get("menu.Next"), Command.SCREEN, 0);
		previousCommand = new Command(Localization.get("menu.Back"), Command.SCREEN, 2);
		viewAnswersCommand = new Command(Localization.get("menu.ViewAnswers"), Command.SCREEN, 3);

		this.addCommand(previousCommand);
		this.addCommand(viewAnswersCommand);
		this.addCommand(nextCommand);
	}

	public void addNavigationButtons()
	{	
		this.append(nextItem);
	    nextItem.setDefaultCommand(nextItemCommand);     // add Command to Item.
	}

	public SingleQuestionScreen (String formTitle) {
        //#style CL_Forms_Form
    	super(formTitle);
	}

	/*
    private void initProgressBar () {
        //#style progressbar
        progressBar = new Gauge(null, false, model.getNumQuestions(), 0);
        this.append(progressBar);
    }
*/
	public void setItemCommandListner(ItemCommandListener itemListner) {
		this.itemListner = itemListner;
	}
	

	public void destroy() {
	}

	public void show() {
			J2MEDisplay.setView(this);
	}

}
