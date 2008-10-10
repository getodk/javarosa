package org.javarosa.patientselect.activity;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.patientselect.object.ExternalizableObject;
import org.javarosa.patientselect.search.BasicSearch;
import org.javarosa.patientselect.store.PatientListMetaData;
import org.javarosa.patientselect.store.PatientStore;

public class PatientListActivity extends MIDlet implements IActivity, CommandListener {
	
	private Context context;
	private List list;
	
	private Command search, save;
	private Alert alert;
	private Gauge patientIndicator;
	
	private IShell parent = null;
	
	private int choiceId = -1;
	
	private ExternalizableObject externObject = null;
	private PatientListMetaData metaDataObject = null;
	private PatientStore store;
	
	private String title = null;
	
	public PatientListActivity(String midTitle){
		
		this.title = midTitle;
		externObject = new ExternalizableObject(title);
		
		store = new PatientStore("PatientStore");
		metaDataObject = new PatientListMetaData();
		
		search = new Command("Search", Command.ITEM, 0);
		save = new Command("Save", Command.ITEM, 1);
	}
	
	public PatientListActivity() {
		// TODO Auto-generated constructor stub
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
		
		list.addCommand(new Command("Exit", Command.EXIT, 1));
		
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
		
		return null;
	}

	public void commandAction(Command command, Displayable display) {
		
		if (command.getCommandType() == Command.EXIT) {
			
			parent.exitShell();
		
		}
		else if(command == List.SELECT_COMMAND){
			
			choiceId = list.getSelectedIndex();
			if(choiceId == 0){
				
				System.out.println(choiceId);
				externObject.initPatientSearchForm();
			}
			
			else if(choiceId == 1){
				
				System.out.println(choiceId);
				externObject.initPatientRegistrationForm();
			}
		}
		
		else if(command.getCommandType() == Command.BACK){
			
			parent.setDisplay(this, list);
			JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		}
		
		else if(command == search){
			
			//#if debug.output == verbose || debug.output == exception
			System.out.println("Entering search");
			//#endif
			
			BasicSearch search = new BasicSearch();
			
			ExternalizableObject externSearch = new ExternalizableObject();
			
			search.searchByCode(externSearch.getPatientCode());
			
			
		}
		else if(command == externObject.getSaveCommand()){
			
			System.out.println("Entering save mode");
			
			Object patData = externObject.getPatientData();
			store.writeToRMS(patData, metaDataObject);	
			
			System.out.println("Exiting save mode [Data saved]");
		}
			else{
				
				String fErrorMessage = "Null Values";
				String sErrorMessage = "Some required patient data is missing on the Patient Form.";
				
				showAlert(fErrorMessage, sErrorMessage);
			}
	}
	
	public void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		
	}
	
	public void pauseApp() {
		// TODO Auto-generated method stub
		
	}
	
	public void startApp() throws MIDletStateChangeException {

		context.setCurrentUser("Mark");
		parent.setDisplay(this, list);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
		
		showList();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}
}
