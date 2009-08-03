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
package org.javarosa.cases.activity;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.cases.util.CaseContext;
import org.javarosa.cases.view.CaseManagementScreen;
import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.services.locale.Localization;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseManagementActivity implements IActivity, CommandListener {
	
	CaseContext context;
	IShell shell;
	
	CaseManagementScreen view;
	
	// Clayton Sims - Mar 19, 2009 : I'm really not a fan of how this is done. Should
	// be refactored at some point.

    public static final String NEW = Localization.get("menu.NewCase");
    public static final String FOLLOWUP = Localization.get("menu.FollowUp");
    public static String REFERRAL = Localization.get("menu.Referral");
    public static final String VIEW_OPEN = Localization.get("menu.ViewOpen");
    public static final String RESOLVE = Localization.get("menu.Resolve");

	Vector commands = new Vector();

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		this.commands.addElement(command);
	}
	
	private void initCommands() {
		Enumeration en = commands.elements();
		while(en.hasMoreElements()) {
			ICommand command = (ICommand)en.nextElement();
			view.addCommand((Command)command.getCommand());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		start(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = new CaseContext(context);
		REFERRAL = Localization.get("menu.Referral",new String[] {String.valueOf(this.context.getNumberOfReferrals())} );
		view = new CaseManagementScreen("Select Action");
		configView();
		view.setCommandListener(this);
		shell.setDisplay(this,view);
	}
	
	private void configView() {
		view.append(NEW, null);
		view.append(FOLLOWUP, null);
		view.append(REFERRAL, null);
		view.append(RESOLVE, null);
		view.append(VIEW_OPEN, null);
		initCommands();
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c.equals(CaseManagementScreen.SELECT_COMMAND)) {
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(Constants.RETURN_ARG_KEY, view.getString(view
					.getSelectedIndex()));
			shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnArgs);
		} else if (c.equals(CaseManagementScreen.BACK)) {
				Hashtable returnArgs = new Hashtable();
				shell.returnFromActivity(this, Constants.ACTIVITY_CANCEL,
						returnArgs);
		} else {
			Hashtable returnArgs = new Hashtable();
			Enumeration annotations = commands.elements();
			while(annotations.hasMoreElements()) {
				ICommand com = (ICommand)annotations.nextElement();
				if(c.equals(com.getCommand())) {
					returnArgs.put(Constants.RETURN_ARG_KEY, com.getCommandId());
					this.shell.returnFromActivity(this,Constants.ACTIVITY_COMPLETE, returnArgs); 
				}
			}
		}
	}
}
