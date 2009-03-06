package org.javarosa.patient.activity;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.patient.activity.view.PatientEditView;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.storage.PatientRMSUtility;

public class EditPatientActivity implements IActivity, CommandListener {

	EditPatientContext context;
	IShell shell;
	PatientEditView view;
	PatientRMSUtility rms;
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		this.context.mergeInContext(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		start(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context){
		view = new PatientEditView("Edit Patient");
		this.context = new EditPatientContext(context);
		rms = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
		Patient pat = new Patient();
		try {
			rms.retrieveFromRMS(this.context.getPatientId(), pat);
			view.setPatient(pat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: Fail if we didn't decode a patient.
		view.setCommandListener(this);
		shell.setDisplay(this, view);
	}

	public void commandAction(Command c, Displayable d) {
		if(c.equals(PatientEditView.CANCEL)) {
			shell.returnFromActivity(this, Constants.ACTIVITY_CANCEL, new Hashtable());
		} else{
			Patient pat = view.getPatient();
			rms.updateToRMS(pat.getRecordId(), pat);
			shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, new Hashtable());
		}
	}

}
