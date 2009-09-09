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

/**
 * 
 */
package org.javarosa.workflow.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.javarosa.core.api.IView;
import org.javarosa.workflow.model.Workflow;
import org.javarosa.workflow.model.WorkflowAction;
import org.javarosa.workflow.model.WorkflowState;

/**
 * The StateLaunchScreen provides a screen that reflects the current
 * state of the workflow, along with the possible actions that can
 * be taken to execute an action from the current state.
 * 
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class StateLaunchScreen extends Form implements IView, CommandListener {
	
	private static final Command BACK =  new Command("Back",Command.BACK,1);
	private static final Command SELECT =  new Command("Select",Command.OK,1);
	
	private Workflow workflow;
	
	public StateLaunchScreen(String title, Workflow workflow) {
		super(title);
		this.workflow = workflow;
		this.addCommand(BACK);
		this.addCommand(SELECT);
				
		populateChoices();
		
		this.setCommandListener(this);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}
	
	private void populateChoices() {
		WorkflowState state = workflow.getCurrentState();
		//TODO: Display this
		state.getDescription();
		
		Vector actions = state.getActions();
		Enumeration en = actions.elements();
		while(en.hasMoreElements()) {
			WorkflowAction action = (WorkflowAction)en.nextElement();
			//TODO: Display this as an option.
			action.getDescription();
		}
	}

	public void commandAction(Command command, Displayable d) {
		if(BACK.equals(command)) {
			workflow.terminate();
		}
		
		if(SELECT.equals(command)) {
			//Get Action.
			//Figure out which action to fire.
			
		}
	}

}
