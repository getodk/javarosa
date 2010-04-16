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
//import java.io.IOException;
//import java.io.InputStream;
//
//import javax.microedition.io.file.FileConnection;
//import javax.microedition.lcdui.Alert;
//import javax.microedition.lcdui.AlertType;
//import javax.microedition.lcdui.Command;
//import javax.microedition.lcdui.CommandListener;
//import javax.microedition.lcdui.Display;
//import javax.microedition.lcdui.Displayable;
//
//import org.javarosa.core.Context;
//import org.javarosa.core.JavaRosaServiceProvider;
//import org.javarosa.core.api.Constants;
//import org.javarosa.core.api.IActivity;
//import org.javarosa.core.api.ICommand;
//import org.javarosa.core.api.IShell;
//import org.javarosa.core.api.IView;
//import org.javarosa.core.model.FormDef;
//import org.javarosa.core.model.storage.FormDefRMSUtility;
//import org.javarosa.xform.util.XFormUtils;
//import org.netbeans.microedition.lcdui.pda.FileBrowser;
//
///**
// * @author Brian DeRenzi
// *
// */
//
////TODO: This class needs to be pulled out of this project (Or at least the part that is dependent
////      on the io.file library that isn't available on a large number of phones)
//public class GetNewFormActivity implements State, CommandListener {
//
//	public static final String FILENAME_KEY = "filename";
//	
//	// #if app.usefileconnections
//	private FileBrowser fileBrowser = null;
//	public String fileBrowserTitle = "Browse XForms";
//	// #endif
//	
//	private IShell parent = null;
//	
//	/** Alert if the form cannot load **/
//	private Alert alert;
//	
//	public GetNewFormActivity(IShell p) {
//		parent = p;
//	}
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
//	 */
//	//@Override
//	public void contextChanged(Context globalContext) {
//		// TODO Auto-generated method stub
//
//	}
//
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#destroy()
//	 */
//	//@Override
//	public void destroy() {
//		// TODO Auto-generated method stub
//
//	}
//
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#getActivityContext()
//	 */
//	//@Override
//	public Context getActivityContext() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#halt()
//	 */
//	//@Override
//	public void halt() {
//		// TODO Auto-generated method stub
//
//	}
//
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
//	 */
//	//@Override
//	public void resume(Context globalContext) {
//		// TODO Auto-generated method stub
//
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
//	 */
//	//@Override
//	public void start(Context context) {
//		// #if app.usefileconnections
//		fileBrowser = new FileBrowser((Display)JavaRosaServiceProvider.instance().getDisplay().getDisplayObject());
//		fileBrowser.setTitle(this.fileBrowserTitle);
//		fileBrowser.setCommandListener(this);
//		fileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
//		fileBrowser.addCommand(FileBrowser.EXIT_COMMAND);
//		JavaRosaServiceProvider.instance().showView(new IView() {public Object getScreenObject() {return fileBrowser;}});
//		// #endif
//	}
//
//	/* (non-Javadoc)
//	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
//	 */
//	//@Override
//	public void commandAction(Command c, Displayable d) {
//		if( d == alert ) {
//			parent.returnFromActivity(this, Constants.ACTIVITY_ERROR, null);
//		}
//		else if( c == FileBrowser.EXIT_COMMAND ) {
//			parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
//		}
//		else if( c == FileBrowser.SELECT_FILE_COMMAND) {
//			// Get ready to dump in the new file
//			FormDefRMSUtility rms = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
//			
//			// Figure out which file was selected
//			try {
//				FileConnection fc = fileBrowser.getSelectedFile();
//				InputStream fis = fc.openInputStream();
//				FormDef form = XFormUtils.getFormFromInputStream(fis);
//				
//				// Display a positive or negative alert
//				if(form == null) {
//					// TODO: internationlize this text
//					displayError("Cannot load form");
//					return;
//				}
//				
//				rms.writeToRMS(form);
//
//				// Exit back
//				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//	}
//	
//	private void displayError(String errorMsg) {
//		alert = new Alert("Form Entry Error",errorMsg,null,AlertType.ERROR);
//		alert.setTimeout(Alert.FOREVER);
//		//setView(alert);
//		//For some reason that I really can't figure out, this alert won't display the error text
//		alert.setCommandListener(this);
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
//	 */
//	public void setShell(IShell shell) {
//		this.parent = shell;
//	}
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
//	 */
//	public void annotateCommand(ICommand command) {
//		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
//	}
//}
