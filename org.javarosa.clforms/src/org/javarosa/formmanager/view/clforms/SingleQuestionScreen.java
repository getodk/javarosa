package org.javarosa.formmanager.view.clforms;

//import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.clforms.widgets.DateQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.NumericQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.Select1QuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.SelectQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TextQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TimeQuestionWidget;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Alert;


public abstract class SingleQuestionScreen extends Form {

	private FormEntryController controller;
	private FormEntryModel model;
	private FormViewManager parent;

	//private Alert alert;
	private boolean multiLingual;
	private QuestionDef prompt;
	private IAnswerData answer;
	
	// GUI elements
	private Gauge progressBar;


	public SingleQuestionScreen(QuestionDef prompt) {
		super(prompt.getName());
		this.prompt = prompt;
	}
	public SingleQuestionScreen (String formTitle) {
        //#style framedForm
    	super(formTitle);
	}
	

	
    private void initProgressBar () {
        //#style progressbar
        progressBar = new Gauge(null, false, model.getNumQuestions(), 0);
        this.append(progressBar);
    }
    

	public void show() {

	}

	public void destroy() {
	}

/*	public void formComplete() {
		progressBar.setValue(model.getNumQuestions());
//		repaint();
		try {
			Thread.sleep(1000); //let them bask in their completeness
		} catch (InterruptedException ie) { }

		controller.save();
		controller.exit();
	}*/
	public boolean checkRequired() 
	{//for now all questions are optional
		return false;
	}
	public void questionIndexChanged(int questionIndex) {

	}

	public void saveStateChanged(int instanceID, boolean dirty) {

	}

	public void setContext(FormEntryContext context) {
		// TODO Auto-generated method stub

	}
	public void setParent(FormViewManager formViewManager) {
		this.parent = formViewManager;

	}
	
    private void showError(String title, String message) {
    	//#style mailAlert
    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
    	alert.setTimeout(Alert.FOREVER);
    	//alert.setCommandListener?
    	Alert.setCurrent(JavaRosaServiceProvider.instance().getDisplay(), alert, null);
    }
	
    public abstract Item initWidget(QuestionDef question);
    public abstract IAnswerData getWidgetValue ();
    public abstract void setHint(String helpText);

}
