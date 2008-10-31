package org.javarosa.communication.http.ui;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.services.transport.TransportMethod;

public class HttpDestinationRetrievalActivity implements IActivity,
		CommandListener {
	
	IShell shell;
	GetURLForm form;

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
		form = new GetURLForm(((HttpTransportMethod) JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(
				new HttpTransportMethod().getId())).getDefaultDestination());
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

}
