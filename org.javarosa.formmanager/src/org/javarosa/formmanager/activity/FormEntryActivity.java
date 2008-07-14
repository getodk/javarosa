package org.javarosa.formmanager.activity;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.model.IControllerListener;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

public class FormEntryActivity implements IActivity, IControllerListener {

	/** View for entering data **/
	private IFormEntryView view;
	
	/** View's controller **/
	private FormEntryController controller;
	
	/** The form that is to be displayed to the user, and its values **/
	private FormEntryModel model;

	/** Current running context **/
	FormEntryContext context;
	
	/** The parent shell **/
	IShell parent;

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
		this.context = (FormEntryContext)context;
		
		//load form
		//parse form
		//pre-process form
		
		if(context.getClass() == FormEntryContext.class) {
			context = (FormEntryContext)context;
			//Set the form definition here
		}
		
		model = new FormEntryModel(/* form data */);
		controller = new FormEntryController(model, this);
		view = new Chatterbox("Chatterbox", model, controller); //shouldn't reference this directly
		
		controller.setView(view);

		//We need to figure out how to identify the View that should be used here.
		//Probably with the properties
	}
	
	
	public void halt () {
		//save displayable
	}
	
	public void resume (Context globalContext) {
		//restore displayable
	}
	
	public void destroy () {
		//initiate destory of m v c
	}
	
	public void setView(Displayable view) {
		parent.setDisplay(this, view);
		
	}
}