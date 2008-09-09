/**
 * 
 */
package org.javarosa.studies;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;

import org.javarosa.core.JavaRosaServiceProvider;

/**
 * @author kaTa
 *
 */
public class StudyUIElements  {
	
	private Form mainForm;
	private List list;
	private Alert alert;
	private Gauge studyIndicator;
	
	private static final String SELECT_STUDY = "Select Study";
	private static final String SELECT_FORM = "Select Form";
	private static final String DOWNLOAD_STUDY_LIST = "Download Study List";
	private static final String DOWNLOAD_FORMS = "Download Forms";
	private static final String UPLOAD_DATA = "Upload Data";
	private static final String SETTINGS = "Settings";
	private static final String LOGOUT = "Logout";
	
	private static final String SETTINGS_GENERAL= "General Settings";
	private static final String SETTINGS_CONNECTION = "Connection Type";
	
	public StudyUIElements(String formTitle){
		
		this.mainForm = new Form(formTitle);
	}

	public List showInitialOptions(){
		
		list = new List("Select an option to continue", Choice.IMPLICIT);
		
		list.append(SELECT_STUDY, null);
		list.append(SELECT_FORM, null);
		list.append(DOWNLOAD_STUDY_LIST, null);
		list.append(DOWNLOAD_FORMS, null);
		list.append(UPLOAD_DATA, null);
		list.append(SETTINGS, null);
		list.append(LOGOUT, null);

		list.addCommand(new Command("Exit", Command.EXIT, 1));
		
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		return list;
		
	}
	
	public List showSettingsForm(){
		
		list = new List("Select an option to continue", Choice.IMPLICIT);
		
		list.append(SETTINGS_GENERAL, null);
		list.append(SETTINGS_CONNECTION, null);
		
		list.addCommand(new Command("Proceed", Command.ITEM, 0));
		list.addCommand(new Command("Back", Command.BACK, 1));
		
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		
		return list;
		
	}
	
	public boolean showAlert(String message, String secMessage) {
		
		alert = new Alert(message, secMessage, null, null);
		
		alert.setTimeout(5000);
		
		studyIndicator = new Gauge(null, false,Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
		
		alert.setIndicator(studyIndicator);
		
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(alert);
		
		return false;
	}
}
