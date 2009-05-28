package org.javarosa.formmanager.view;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.transport.TransportMessage;

public class SubmitStatusScreen extends Form implements ISubmitStatusScreen {
	private int modelID;
    private StringItem msg;
    private Command okCommand;
    private Timer timer;
    int counter = 0;

    public static final int REFRESH_INTERVAL = 1000;
    public static final int TIMEOUT = 60000;

    
	public static final String MSG_SUCCESS = JavaRosaServiceProvider.instance().localize("view.sending.SuccessfullySubmitted");
	public static final String MSG_FAILED = JavaRosaServiceProvider.instance().localize("view.sending.SubmissionFailed");
	public static final String MSG_TOO_LONG = JavaRosaServiceProvider.instance().localize("view.sending.SubmissionTakingLong");
	public static final String MSG_UNKNOWN_ERROR = JavaRosaServiceProvider.instance().localize("view.sending.SubmissionErrorUnknown");
	public static String MSG_SENDING =  JavaRosaServiceProvider.instance().localize("view.sending.SendingInProgress");
    
    public SubmitStatusScreen (CommandListener listener) {
    	this(listener, -1);
	}
    
	public SubmitStatusScreen (CommandListener listener, int modelID) {
    	//#style submitPopup
		super("Send Status");
		this.modelID = modelID;
		setCommandListener(listener);
		
		okCommand = new Command("OK", Command.OK, 1);
		msg = new StringItem(null, MSG_SENDING);

		addCommand(okCommand);
		append(new Spacer(80, 0));
		append(msg);	

		timer = new Timer();
		timer.schedule(new TimerTask () {
			public void run () {
				updateStatus();
			}
		}, REFRESH_INTERVAL, REFRESH_INTERVAL);
	}
	
	public void updateStatus () {
		int status = JavaRosaServiceProvider.instance().getTransportManager().getModelDeliveryStatus(modelID, false);
		updateStatus(status);
	}
	
	public void updateStatus(int status) {
			counter += REFRESH_INTERVAL;

			if (status != TransportMessage.STATUS_NEW)
				timer.cancel();

			String message;
			switch (status) {
			case TransportMessage.STATUS_NEW:       message = (counter < TIMEOUT ? MSG_SENDING : MSG_TOO_LONG); break;
			case TransportMessage.STATUS_DELIVERED: message = MSG_SUCCESS + getServerResponse(); break;
			case TransportMessage.STATUS_FAILED:    message = MSG_FAILED; break;
			default:                                message = MSG_UNKNOWN_ERROR; break;
			}

			msg.setText(message);
	}
	
	public void destroy () {
		timer.cancel();
	}

	public String getServerResponse()  {
		Enumeration messages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
		String receipt ="";
		//while (messages.hasMoreElements())
		//{
		TransportMessage response = (TransportMessage) messages.nextElement();
		receipt = new String(response.getReplyloadData()); //this does not seem terribly robust
		//}
		return receipt;
	}
	public Object getScreenObject() {
		return this;
	}
}



