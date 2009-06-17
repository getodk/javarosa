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


package org.javarosa.formmanager.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.Map;
import org.javarosa.formmanager.activity.FormListActivity;

 
public class FormList extends List implements CommandListener, IView {
	
	private final static Command CMD_EXIT = new Command(JavaRosaServiceProvider.instance().localize("menu.Exit"), Command.BACK, 2);
	private final static Command CMD_GETNEWFORMS = new Command(JavaRosaServiceProvider.instance().localize("menu.GetNewForms"), Command.SCREEN, 2);
    // CZUE added for testing 
	private final static Command CMD_CAMERA = new Command(JavaRosaServiceProvider.instance().localize("menu.Camera"), Command.SCREEN, 2);
    //Added for debugging(Ndubisi)
	private final static Command CMD_RECORDER = new Command(JavaRosaServiceProvider.instance().localize("menu.Recorder"), Command.SCREEN, 2);
	private final static Command CMD_IMAGE_BROWSE = new Command(JavaRosaServiceProvider.instance().localize("menu.ImageManagement"), Command.SCREEN, 2);
	// others
	private final static Command CMD_VIEWMODELS = new Command(JavaRosaServiceProvider.instance().localize("menu.ViewSaved"), Command.SCREEN, 3);
	private final static Command CMD_DELETE_FORM = new Command(JavaRosaServiceProvider.instance().localize("menu.Delete"),Command.SCREEN,4);
	private final static Command CMD_SHAREFORMS = new Command(JavaRosaServiceProvider.instance().localize("menu.ShareForms"), Command.SCREEN, 2);
	private final static Command CMD_SETTINGS = new Command(JavaRosaServiceProvider.instance().localize("menu.Settings"), Command.SCREEN, 3);
	
    private FormListActivity parent = null;

	public FormList(FormListActivity p, String title) {
		this(p, title, Choice.IMPLICIT);
	}

	public FormList(FormListActivity p, String title, int listType) {
		super(title, listType);
		this.parent = p;
	}

	/**
	 * 
	 * @param formsPositionTitleMap
	 * @return Vector of form positions
	 */
	public Vector loadView(Map formsPositionTitleMap) {
		// delete all elements of this list
		this.deleteAll();
		// create view by adding screen commands
		this.createView();
		this.setCommandListener(this);
		return this.populateWithXForms(formsPositionTitleMap);
	}

	private void createView() {
		addScreenCommands();
	}

	private void addScreenCommands() {
//		Context c = this.parent.getContext();
//		//boolean demo = false;
//		if(c.getElement("USER")!=null){
//			//User loggedInUser = (User)c.getElement("USER");
//			//if (loggedInUser.getUserType().equals(User.DEMO_USER))
//				//demo = true;
//		}
		this.addCommand(CMD_EXIT);
        //this.addCommand(CMD_OPEN);
        this.addCommand(CMD_DELETE_FORM);
        this.addCommand(CMD_VIEWMODELS);
        this.addCommand(CMD_GETNEWFORMS);
        this.addCommand(CMD_CAMERA);
        this.addCommand(CMD_RECORDER);
        this.addCommand(CMD_IMAGE_BROWSE);
//        this.addCommand(CMD_SHAREFORMS);

        //#if polish.usePolishGui
        this.addCommand(CMD_SETTINGS);
        //#endif
        
        for (int i = 0; i < this.parent.getCustomCommands().size(); i++) {
        	Command parentCommand = (Command)((ICommand)this.parent.getCustomCommands().elementAt(i)).getCommand();
        	addCommand(parentCommand);
        }
	}

	 
	/**
	 * add form titles to this view, in order
	 * 
	 * @param formsPositionTitleMap maps ids to form titles
	 * @return Vector of form positions
	 */
	private Vector populateWithXForms(Map formsPositionTitleMap) {
		Vector formPositions = new Vector();
		Enumeration e = formsPositionTitleMap.keys();
		while(e.hasMoreElements()){
			Integer pos = (Integer)e.nextElement();
			String formTitle = (String)formsPositionTitleMap.get(pos);
			this.append(formTitle,null);
			formPositions.addElement(pos);
		}
		return formPositions;
	}


	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable arg1) {
		
		if (c == List.SELECT_COMMAND) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_SELECT_XFORM, new Integer(this.getSelectedIndex()));
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}

        if (c == CMD_DELETE_FORM) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_DELETE_FORM, new Integer(this.getSelectedIndex()));
			// TODO check for any form dependancies
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}

		else if (c == CMD_EXIT) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_EXIT, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}

		else if (c == CMD_GETNEWFORMS) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_GET_NEW_FORM, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}

		else if (c == CMD_VIEWMODELS) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_VIEW_DATA, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}
        // CZUE: camera test
		else if (c == CMD_CAMERA) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_CAMERA, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}
		else if (c == CMD_IMAGE_BROWSE) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_IMAGE_BROWSE, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}

		else if (c == this.parent.CMD_ADD_USER) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_ADD_USER, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}
        //Recorder debugging(Ndubisi)
		else if(c == CMD_RECORDER)
		{
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_RECORDER, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}
        
        /*
		if (c == CMD_SHAREFORMS) {
			this.mainShell.startBToothClient();
		}
		*/
		else if (c == CMD_SETTINGS) {
			Hashtable commands = new Hashtable();
			commands.put(Commands.CMD_SETTINGS, "");
			this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);
		}
		//this case should be triggered when new custom commands are added
		else {
			for (int i = 0; i < this.parent.getCustomCommands().size(); i++) {
				ICommand parentCommand = (ICommand)this.parent.getCustomCommands().elementAt(i);
				if (c == parentCommand.getCommand()) {
					Hashtable commands = new Hashtable();
					commands.put(parentCommand.getCommandId(), "");
					this.parent.viewCompleted(commands, ViewTypes.FORM_LIST);		
				}
			}			
		}
	}
	public Object getScreenObject() {
		return this;
	}

}
