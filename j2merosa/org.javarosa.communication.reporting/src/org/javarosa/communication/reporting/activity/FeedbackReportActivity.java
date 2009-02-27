/**
 * 
 */
package org.javarosa.communication.reporting.activity;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.reporting.properties.FeedbackReportProperties;
import org.javarosa.communication.reporting.view.FeedbackReportScreen;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.TransportMessage;

/**
 * @author Clayton Sims
 * @date Feb 27, 2009 
 *
 */
public class FeedbackReportActivity implements IActivity, CommandListener {
	
	FeedbackReportScreen screen;
	IShell shell;
	Context context;
	

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
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
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		this.contextChanged(globalContext);
		this.start(globalContext);
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
		screen = new FeedbackReportScreen("");
		screen.setCommandListener(this);
		shell.setDisplay(this, screen);
	}

	public void commandAction(Command c, Displayable d) {
		if(c.equals(FeedbackReportScreen.SEND_REPORT)) {
			String message = screen.getString();
			
			String url = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(FeedbackReportProperties.FEEDBACK_REPORT_SERVER);
			
			String id = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty("DeviceID");
			
			//TODO: For now, only http is supported, so we hack this to switch to that.
			int httpmethod = (new HttpTransportMethod()).getId();
			JavaRosaServiceProvider.instance().getTransportManager().setCurrentTransportMethod(httpmethod);
			
			TransportMessage tmessage = new TransportMessage(new ByteArrayPayload(message.getBytes(), "Feedback Message",IDataPayload.PAYLOAD_TYPE_TEXT), new HttpTransportDestination(url),id,-1);
			JavaRosaServiceProvider.instance().getTransportManager().send(tmessage, httpmethod);
			
			//TODO: Feedback!
			
			shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, new Hashtable());
		} else if (c.equals(FeedbackReportScreen.CANCEL)) {
			shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, new Hashtable());
		}
	}

}
