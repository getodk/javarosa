package org.javarosa.patientselect.activity;

import java.util.Vector;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;

import org.javarosa.patientselect.object.*;

public class PatientListActivity extends MIDlet implements IActivity, CommandListener {
	
	private Context context;
	private List list;
	
	private Command search;
	private Alert alert;
	private Gauge patientIndicator;
	
	private IShell parent = null;
	
	private int choiceId = -1;
	private int formId;
	
	private ExternalizableObject externObject = null;
	
	private String title = null;
	
	public PatientListActivity(String midTitle){
		
		this.title = midTitle;
		externObject = new ExternalizableObject(title);
		
	}
	
	public void contextChanged(Context globalContext) {
		
		Vector contextChanges = this.context.mergeInContext(context);
		contextChanges.capacity();
		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void halt() {
		// TODO Auto-generated method stub
		
	}

	public void resume(Context globalContext) {
		
		this.contextChanged(context);
		JavaRosaServiceProvider.instance().showView(this.list);
		
	}

	public void start(Context context) {
		
		this.context = context;
		showList();
		
	}

	public List showList() {
		
		list = new List("Select an option to continue", Choice.IMPLICIT);
		
		list.append("Search Patient", null);
		list.append("Enter New Patient", null);
		
		list.addCommand(new Command("Proceed", Command.SCREEN, 0));
		list.addCommand(new Command("Exit", Command.EXIT, 1));
		
		//parent.setDisplay(this, list);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		
		list.setCommandListener(this);
		
		return list;
	}

	private boolean showAlert(String message, String secMessage) {
		
		alert = new Alert(message, secMessage, null, null);
		
		alert.setTimeout(5000);
		
		patientIndicator = new Gauge(null, false,Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
		
		alert.setIndicator(patientIndicator);
		
		//parent.setDisplay(this, alert);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(alert);
		
		return false;
	}

	public String selectPatient(String patientId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getFormId() {
		
		return formId;
	}

	public void commandAction(Command command, Displayable display) {
		
		if (command.getCommandType() == Command.EXIT) {
			
			parent.exitShell();
		
		}
		else if(command.getCommandType() == Command.SCREEN){
			
			choiceId = list.getSelectedIndex();
			
			if(choiceId == 0){
				
				System.out.println(choiceId);
				
			}
			
			else if(choiceId == 1){
				
				System.out.println(choiceId);
				
			}
		}
		
		else if(command.getCommandType() == Command.BACK){
			
			parent.setDisplay(this, list);
			JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		}
		
		else if(command == search){
			
			
		}
		else if(command.getCommandType() == Command.ITEM){
			
			System.out.println("Entering save mode");
			
			System.out.println("Entering save mode in [LaunchActivity]");
			
			System.out.println("Exiting save mode [Data saved]");
		}
	}
	
	public void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		
	}
	
	public void pauseApp() {
		// TODO Auto-generated method stub
		
	}
	
	public void startApp() throws MIDletStateChangeException {
		
		this.context = context;
		parent.setDisplay(this, list);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		
		showList();
		
	}


}
