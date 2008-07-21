package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.controller.IControllerHost;
import org.javarosa.formmanager.view.IFormEntryView;

public class FormEntryActivity implements IActivity, IControllerHost, CommandListener {

	/** Alert if the form cannot load **/
	private Alert alert;
	
	/** View for entering data **/
	private IFormEntryView view;
	
	/** View's controller **/
	private FormEntryController controller;
	
	/** The form that is to be displayed to the user, and its values **/
	private FormEntryModel model;

	/** Current running context **/
	private FormEntryContext context;
	
	/** The parent shell **/
	private IShell parent;
	
	/** Loading error string **/
	private final static String LOAD_ERROR = "Deepest Apologies. The form could not be loaded.";

	public FormEntryActivity(IShell parent) {
		this.parent = parent;
	}
	
	public void contextChanged(Context context) {
		Vector contextChanges = this.context.mergeInContext(context);
		
		Enumeration en = contextChanges.elements();
		while(en.hasMoreElements()) {
			String changedValue = (String)en.nextElement();
			if(changedValue == Constants.USER_KEY) {
				//Do we need to update the username?
			}
		}
	}
	
	public void start (Context context) {
		FormDef theForm = null;
		if(context.getClass() == FormEntryContext.class) {
			this.context = (FormEntryContext)context;			
			
			//TODO: Are we going to make this non-RMS dependant any any point?
			FormDefRMSUtility utility = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
			theForm = new FormDef();
			try {
				utility.retrieveFromRMS(this.context.getFormID(),theForm);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (UnavailableExternalizerException uee) {
				uee.printStackTrace();
			}
		}
		if (theForm != null) {

			// pre-process form

			model = new FormEntryModel(theForm);
			controller = new FormEntryController(model, this);
			//TODO: Figure out what view to use
			//view = new Chatterbox("Chatterbox", model, controller); // shouldn't
																	// reference
																	// this
																	// directly

			//controller.setView(view);

			// We need to figure out how to identify the View that should be
			// used here.
			// Probably with the properties
			
			view.show();
		} else {
			displayError(LOAD_ERROR);
		}
	}
	
	
	public void halt () {
		//need to do anything?
	}
	
	public void resume (Context globalContext) {
		view.show();
	}
	
	public void destroy () {
		//initiate destory of m v c
	}
	
	public void setDisplay (Displayable d) {
		parent.setDisplay(this, d);
	}
	
	public void controllerReturn (String status) {
	}
	
	public void commandAction(Command command, Displayable display) {
		if(display == alert) {
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
		}
	}
	
	private void displayError(String errorMsg) {
		alert = new Alert("Form Entry Error",errorMsg,null,AlertType.ERROR);
		alert.setTimeout(Alert.FOREVER);
		//setView(alert);
		//For some reason that I really can't figure out, this alert won't display the error text
		alert.setCommandListener(this);
	}
}