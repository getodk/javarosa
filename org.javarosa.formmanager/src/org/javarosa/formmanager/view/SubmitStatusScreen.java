package org.javarosa.formmanager.view;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.formmanager.activity.FormTransportActivity;

public class SubmitStatusScreen extends Form  {
	private int modelID;
    private StringItem msg;
    private Command okCommand;
    private Timer timer;
    int counter = 0;

    public static final int REFRESH_INTERVAL = 1000;
    public static final int TIMEOUT = 60000;

    public static String MSG_SENDING = "Sending...";
    public static String MSG_SUCCESS = "Form has been submitted successfully! Your reference is: ";
    public static String MSG_FAILED = "Submission failed! Please try to submit the form again later in 'View Saved'.";
    public static String MSG_TOO_LONG = "Sending is taking a long time; you may check on the status and/or resend later in 'View Saved'";
    public static String MSG_UNKNOWN_ERROR = "Unknown sending error; form not sent!";
    
	public SubmitStatusScreen (FormTransportActivity fta, int modelID) {
    	//#style submitPopup
		super("Send Status");
		this.modelID = modelID;
		setCommandListener(fta);
		
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
		counter += REFRESH_INTERVAL;

		int status = JavaRosaServiceProvider.instance().getTransportManager().getModelDeliveryStatus(modelID, false);
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
}



