///*
// * Copyright (C) 2009 JavaRosa
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//
///**
// *
// */
//package org.javarosa.formmanager.activity;
//
//import java.util.Enumeration;
//import java.util.Hashtable;
//import java.util.Vector;
//
//import javax.microedition.lcdui.Command;
//
//import org.javarosa.core.Context;
//import org.javarosa.core.JavaRosaServiceProvider;
//import org.javarosa.core.api.Constants;
//import org.javarosa.core.api.IActivity;
//import org.javarosa.core.api.ICommand;
//import org.javarosa.core.api.IShell;
//import org.javarosa.core.model.storage.FormDefMetaData;
//import org.javarosa.core.model.storage.FormDefRMSUtility;
//import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
//import org.javarosa.core.services.storage.utilities.RecordStorageException;
//import org.javarosa.core.util.Map;
//import org.javarosa.formmanager.view.Commands;
//import org.javarosa.formmanager.view.FormList;
//import org.javarosa.formmanager.view.ViewTypes;
//import org.javarosa.user.model.User;
//
//
//
///**
// * @author Brian DeRenzi
// * 
// *         Activity when user selects to view forms
// * 
// * 
// */
//public class FormListActivity implements IActivity {
//
//	public static final String COMMAND_KEY = "command";
//	public static final String FORM_ID_KEY = "form_id";
//	// refactor these out.
//	public final Command CMD_ADD_USER = new Command("Add User", Command.SCREEN,
//			5);
//	public final Command CMD_SAVE = new Command("Save", Command.OK, 1);
// 
//	 
// 
//	
//	// calling shell
//	 
//	 
//	// the view, which for this activity is a FormList
//	private FormList view = null;
//
//	// the structures into which the forms are read
//	private Vector forms = new Vector();
//	private Map formsPositionTitleMap = new Map();
//	private Vector formPositions = null;
//	
//	 
//	private IShell parent = null;
//
//	private Context context;
//
//	// TODO: what type are the elements?
//	private Vector customCommands = new Vector();
//
//	private FormDefRMSUtility formDefRMSUtility = null;
//
//	/**
//	 * 
//	 * @param p
//	 *            shell from which this activity is called
//	 * @param title
//	 *            title of the activity
//	 */
//	public FormListActivity(IShell p, String title) {
//		this.parent = p;
//		this.view = new FormList(this, title);
//	}
//
//	public void start(Context ctx) {
//		this.context = ctx;
//
//		initFormsRMSInterface();
//		
//		readXFormsFromRMS();
//
//		// if there is a logged in user, add the command "ADD USER"
//		// to the formsList (?)
//		if (ctx.getElement("USER") != null) {
//			User loggedInUser = (User) ctx.getElement("USER");
//			if (loggedInUser.isAdminUser())
//				this.view.addCommand(this.CMD_ADD_USER);
//		}
//
//		this.formPositions = this.view.loadView(this.formsPositionTitleMap);
//		this.parent.setDisplay(this, this.view);
//	}
//
//
//	/**
//	 * 
//	 * method called following completion (exit?) from the view
//	 * 
//	 * @param commands
//	 *            Command-Integer hash
//	 * @param viewCompleted
//	 *            the view which has just been completed
//	 */
//	public void viewCompleted(Hashtable commands, int viewCompleted) {
//		// Determine which view just completed and act accordingly
//		switch (viewCompleted) {
//		case ViewTypes.FORM_LIST:
//			processCommandsFromView(commands);
//			break;
//		}
//	}
//
//	public void contextChanged(Context context) {
//		Vector contextChanges = this.context.mergeInContext(context);
//
//		Enumeration en = contextChanges.elements();
//		while (en.hasMoreElements()) {
//			String changedValue = (String) en.nextElement();
//			if (changedValue == Constants.USER_KEY) {
//				// TODO
//				// update username somewhere
//			}
//		}
//	}
//
//	
//
//	public void resume(Context context) {
//		this.contextChanged(context);
//		// Possibly want to check for new/updated forms
//		JavaRosaServiceProvider.instance().showView(this.view);
//	}
//
//
//
//	public void addNewMenuCommand(Command c) {
//		this.view.addCommand(c);
//	}
//	
//
//	// TODO - type of elements in customCommands?
//	public void addCustomCommand(String command){
//		this.customCommands.addElement(command);
//	}
//	public void annotateCommand(ICommand command) {
//		this.customCommands.addElement(command);
//	}
//	
//	
//	
//	// getters
//	public Context getActivityContext() {
//		return this.context;
//	}
//	public Context getContext() {
//		return this.context;
//	}
//	public Vector getCustomCommands(){
//		return this.customCommands;
//	}
//	
//	// setters
//	public void setShell(IShell shell) {
//		this.parent = shell;
//	}
//
//	public void destroy() {
//		// do nothing?
//	}
//	public void halt() {
//		// do nothing?
//	}
//	
//	/**
//	 * @return the FormDefRMSUtility
//	 */
//	private FormDefRMSUtility initFormsRMSInterface() {
//		if (this.formDefRMSUtility == null) {
//			this.formDefRMSUtility = (FormDefRMSUtility) JavaRosaServiceProvider
//					.instance().getStorageManager().getRMSStorageProvider()
//					.getUtility(FormDefRMSUtility.getUtilityName());
//			this.formDefRMSUtility.open();
//		}
//		return this.formDefRMSUtility;
//	}
//	
//	/**
//	 * 
//	 * Select a form (metadata) from the forms read in, according to the params
//	 * 
//	 * @param command
//	 * @param commands
//	 * @return
//	 */
//	private FormDefMetaData getSelectedForm(String command, Hashtable commands) {
//		int selectedPosition = ((Integer) commands.get(command)).intValue();
//		int formPosition = ((Integer) this.formPositions
//				.elementAt(selectedPosition)).intValue();
//		// #if debug.output==verbose
//		System.out.println("Selecting form: "
//				+ this.forms.elementAt(formPosition) + " with command "
//				+ command);
//		// #endif
//		FormDefMetaData meta = (FormDefMetaData) this.forms
//				.elementAt(formPosition);
//		return meta;
//	}
//
//	/**
//	 * 
//	 * process the commands
//	 * 
//	 * 
//	 * @param commands
//	 *            Command-Integer hash
//	 */
//	private void processCommandsFromView(Hashtable commands) {
//		Enumeration en = commands.keys();
//		while (en.hasMoreElements()) {
//			String cmd = (String) en.nextElement();
//
//			
//			// select a form
//			if (cmd == Commands.CMD_SELECT_XFORM) {
//				FormDefMetaData meta = getSelectedForm(
//						Commands.CMD_SELECT_XFORM, commands);
//				parentReturnWithFormIdAndCommand(meta.getRecordId(),
//						Commands.CMD_SELECT_XFORM);
//				return;
//			} 
//			
//			
//			// delete a form
//			if (cmd == Commands.CMD_DELETE_FORM) {
//				FormDefMetaData formMetaData = getSelectedForm(
//						Commands.CMD_DELETE_FORM, commands);
//
//				FormDefRMSUtility rms = initFormsRMSInterface();
//				rms.deleteRecord(formMetaData.getRecordId());
//				
//				System.out.println("deleted form " + formMetaData.getRecordId());
//
//				// refresh following deletion
//				this.start(this.context);
//				
//				return;
//			} 
//
//			// other commands
//			if (cmd == Commands.CMD_ADD_USER) {
//				parentReturnWithCommand(Commands.CMD_ADD_USER);
//			} else if (cmd == Commands.CMD_EXIT) {
//				parentReturnWithCommand(Commands.CMD_EXIT);
//			} else if (cmd == Commands.CMD_VIEW_DATA) {
//				parentReturnWithCommand(Commands.CMD_VIEW_DATA);
//			} else if (cmd == Commands.CMD_SETTINGS) {
//				parentReturnWithCommand(Commands.CMD_SETTINGS);
//			} else if (cmd == Commands.CMD_GET_NEW_FORM) {
//				// Using activity complete means that the shell must actively
//				// recreate the
//				// forms list. I think this is best to ensure that the list is
//				// updated
//				// - Brian DeRenzi 18 Aug 2008
//
//				parentReturnWithCommand(Commands.CMD_GET_NEW_FORM);
//
//			} else {
//				parentReturnWithCommand(cmd);
//			}
//		}
//	}
//
//	/**
//	 * 
//	 * invoke returnFromActivity from the parent shell, passing the command executed
//	 * 
//	 * @param command
//	 */
//	private void parentReturnWithCommand(String command) {
//		Hashtable returnArgs = new Hashtable();
//		returnArgs.put(COMMAND_KEY, command);
//		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
//				returnArgs);
//	}
//
//	/**
//	 * 
//	 * invoke returnFromActivity from the parent shell, passing the command executed
//	 * and the formid operated on
//	 * 
//	 * @param id
//	 * @param command
//	 */
//	private void parentReturnWithFormIdAndCommand(int id, String command) {
//		Hashtable returnArgs = new Hashtable();
//		returnArgs.put(COMMAND_KEY, command);
//		returnArgs.put(FORM_ID_KEY, new Integer(id));
//		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
//				returnArgs);
//	}
//
//	/**
//	 * 
//	 * Get forms from RMS and insert them into instance variables (title and id)
//	 * 
//	 */
//	private void readXFormsFromRMS() {
//
//		IRecordStoreEnumeration formsIds = this.formDefRMSUtility
//				.enumerateMetaData();
//		int pos = 0;
//		while (formsIds.hasNextElement()) {
//			int id;
//			try {
//				id = formsIds.nextRecordId();
//				FormDefMetaData formMetadata = retrieveForm(id);
//				// TODO fix it so that record id is part of the metadata
//				// serialization
//
//				// BWD 27/7/2008
//				// Getting rid of annoying numbers thing
//				// listOfForms.put(new Integer(pos),
//				// mdata.getRecordId()+"-"+mdata.getName());
//				this.formsPositionTitleMap.put(new Integer(pos), formMetadata
//						.getTitle());
//				this.forms.insertElementAt(formMetadata, pos);
//				pos++;
//			} catch (RecordStorageException e) {
//				// #if debug.output==verbose || debug.output==exception
//				e.printStackTrace();
//				// #endif
//			}
//		}
//	}
//
//	/**
//	 * 
//	 * retrieve form with specific id from RMS
//	 * 
//	 * @param id
//	 * @param formDefRMSUtility
//	 * @return
//	 */
//	private FormDefMetaData retrieveForm(int id) {
//		FormDefMetaData formMetadata = new FormDefMetaData();
//		this.formDefRMSUtility.retrieveMetaDataFromRMS(id, formMetadata);
//		return formMetadata;
//	}
//
//	
//
//	
//}
//
// 
