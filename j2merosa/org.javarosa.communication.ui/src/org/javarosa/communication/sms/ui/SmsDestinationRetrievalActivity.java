package org.javarosa.communication.sms.ui;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.ui.GetURLForm;
import org.javarosa.communication.sms.SmsTransportMethod;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.services.transport.TransportMethod;

public class SmsDestinationRetrievalActivity implements IActivity, CommandListener {
	
	IShell shell;
	GetSmsURLForm form;

	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub
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
		shell.setDisplay(this, form);
	}

	public void start(Context context) {
		form = new GetSmsURLForm(((SmsTransportMethod) JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(
				new SmsTransportMethod().getId())).getDefaultDestination());
		form.setCommandListener(this);
		shell.setDisplay(this, form);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		Hashtable returnArgs = new Hashtable();
		if(arg0 == GetURLForm.CMD_OK) {
			returnArgs.put(TransportMethod.DESTINATION_KEY, new HttpTransportDestination(form.getDestination()));
		} else {
			returnArgs.put(TransportMethod.DESTINATION_KEY, null);
		}
		form = null;
		shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
