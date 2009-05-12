/**
 * 
 */
package org.javarosa.log.activity;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.log.FlatLogSerializer;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.log.properties.LogPropertyRules;
import org.javarosa.log.view.LogManagementView;
import org.javarosa.log.view.LogViewer;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogManagementActivity implements IActivity, CommandListener {
	
	public static final String CLEAR_LOGS = "Clear Logs";
	public static final String VIEW_LOGS = "View Logs";
	public static final String SEND_LOGS = "Send Logs";
	
	public static final Command EXIT = new Command("Back",Command.BACK, 0);
	
	Context context;
	IShell shell;
	LogManagementView manager;
	LogViewer viewer;
	
	public LogManagementActivity() {
		String a = "as";
	}

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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

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
	public void start(Context context) {
		this.context = context;
		
		manager = new LogManagementView();
		manager.append(VIEW_LOGS, null);
		manager.append(SEND_LOGS, null);
		manager.append(CLEAR_LOGS, null);
		
		manager.addCommand(EXIT);
		
		manager.setCommandListener(this);

		shell.setDisplay(this, manager);
	}

	public void commandAction(Command com, Displayable d) {
		if (d instanceof Alert) {
			shell.setDisplay(this, manager);
		}
		if (d == manager) {
			String action = manager.getString(manager.getSelectedIndex());
			if (action.equals(CLEAR_LOGS)) {
				JavaRosaServiceProvider.instance().getIncidentLogger()
						.clearLogs();
		    	//#style mailAlert
		    	final Alert success = new Alert("Logs Cleared", "Logs cleared succesfully", null, AlertType.CONFIRMATION);
				success.setTimeout(Alert.FOREVER);
				IView successalert = new IView() {
					public Object getScreenObject() {
						return success;
					}
				};
				shell.setDisplay(this, successalert);
			} else if (action.equals(VIEW_LOGS)) {
				viewer = new LogViewer();
				byte[] logData = JavaRosaServiceProvider.instance().getIncidentLogger().serializeLogs(new FlatLogSerializer());
				viewer.loadLogs(new String(logData));
				viewer.setCommandListener(this);
				viewer.addCommand(EXIT);
				shell.setDisplay(this, viewer);
			} else if (action.equals(SEND_LOGS)) {
				byte[] logData = JavaRosaServiceProvider.instance().getIncidentLogger().serializeLogs(new FlatLogSerializer());
				ByteArrayPayload payload = new ByteArrayPayload(logData,"",IDataPayload.PAYLOAD_TYPE_TEXT);
				HttpTransportDestination destination = new HttpTransportDestination(JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(LogPropertyRules.LOG_SUBMIT_URL));
				try {
					JavaRosaServiceProvider.instance().getTransportManager().enqueue(payload, destination, JavaRosaServiceProvider.instance().getTransportManager().getCurrentTransportMethod(), 0);
					//#style mailAlert
			    	final Alert sending = new Alert("Sending Started", "Log Sending Started", null, AlertType.ERROR);
			    	sending.setTimeout(Alert.FOREVER);
					IView successalert = new IView() {
						public Object getScreenObject() {
							return sending;
						}
					};
					shell.setDisplay(this, successalert);
					
				} catch (IOException e) {
					//#style mailAlert
			    	final Alert failure = new Alert("Send Failed", "Log sending failure", null, AlertType.ERROR);
			    	failure.setTimeout(Alert.FOREVER);
					IView successalert = new IView() {
						public Object getScreenObject() {
							return failure;
						}
					};
					shell.setDisplay(this, successalert);
				}
			}
			if(com.equals(EXIT)) {
				shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, new Hashtable());
			}
		}
		else if(d == viewer) {
			shell.setDisplay(this, manager);
		}
	}
}
