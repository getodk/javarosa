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
package org.javarosa.workflow.activity;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.workflow.WorkflowLaunchContext;
import org.javarosa.workflow.model.IWorkflowActionListener;
import org.javarosa.workflow.model.Workflow;
import org.javarosa.workflow.model.WorkflowAction;
import org.javarosa.workflow.view.StateLaunchScreen;

/**
 * WorkflowLaunchActivity is responsible for identifying the current state of a
 * workflow and presenting the appropriate User Interface for that state.
 * 
 * @author Clayton Sims
 * @date Jan 13, 2009
 * 
 */
public class WorkflowActivity implements IActivity, IWorkflowActionListener {

	/**
	 * The workflow that will be used in this activity.
	 */
	Workflow workflow;

	WorkflowLaunchContext context;

	IShell shell;

	StateLaunchScreen stateScreen;

	/** Vector<Command> **/
	Vector commands = new Vector();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		shell.setDisplay(this, stateScreen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		if (context instanceof WorkflowLaunchContext) {
			this.context = (WorkflowLaunchContext) context;
			this.workflow = this.context.getWorkflow();
			this.workflow.setDataModel(this.context.getDataModel());
		}
		stateScreen = new StateLaunchScreen("Title", this.workflow);
		Enumeration en = commands.elements();
		while(en.hasMoreElements()) {
			stateScreen.addCommand((Command)en.nextElement());
		}
		shell.setDisplay(this, stateScreen);
	}

	public void actionFired(WorkflowAction action) {
		Hashtable returnArgs = new Hashtable();
		returnArgs.put(Constants.RETURN_ARG_KEY, action);
		shell.returnFromActivity(this, Constants.ACTIVITY_SUSPEND, returnArgs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api
	 * .ICommand)
	 */
	public void annotateCommand(ICommand command) {
		if (command.getCommand() instanceof Command) {
			this.commands.addElement(command.getCommand());
		} else {
			throw new RuntimeException(
					"Attempted to annotate a platform invalid command of class "
							+ command.getCommand().getClass().toString()
							+ " to the j2me platform Activity WorkflowActivity");
		}
	}
}
