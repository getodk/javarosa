package org.javarosa.formmanager.activity;

import org.javarosa.core.*;
import org.javarosa.core.api.*;
import org.javarosa.formmanager.model.*;
import org.javarosa.formmanager.controller.*;
import org.javarosa.formmanager.view.*;
import org.javarosa.formmanager.view.chatterbox.*;

public class FormEntryActivity implements IActivity {
	IShell shell;
	FormEntryContext context;
	FormEntryModel model;
	FormEntryController controller;
	IFormEntryView view;
	
	public FormEntryActivity (IShell shell) {
		this.shell = shell;
	}
	
	public void start (Context context) {
		this.context = (FormEntryContext)context;
		
		//load form
		//parse form
		//pre-process form
		
		model = new FormEntryModel(/* form data */);
		controller = new FormEntryController(model);
		view = new Chatterbox("Chatterbox", model, controller); //shouldn't reference this directly

		//shell.setDisplay(this, view); //how to do this?
	}
	
	public void contextChanged (Context globalContext) {
		
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
}