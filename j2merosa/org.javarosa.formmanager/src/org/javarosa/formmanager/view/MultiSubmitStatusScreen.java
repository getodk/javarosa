/**
 * 
 */
package org.javarosa.formmanager.view;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.transport.TransportMessage;

/**
 * Note: This screen assumes that the model IDs provided will be sent in a more or
 * less sequential fashion.
 * 
 * @author Clayton Sims
 * @date Jan 11, 2009 
 *
 */
public class MultiSubmitStatusScreen extends Form implements ISubmitStatusScreen {
	
	private int currentid;
	private int[] modelIDs;
    private StringItem msg;
    private Command okCommand;
    private Timer timer;
    int counter = 0;
    
    private String response;

    public static final int REFRESH_INTERVAL = 1000;
    public static final int TIMEOUT = 180000;

    //#if commcare.lang.sw
    public static final String MSG_N_COMP = "Umefanikiwa kutuma Fomu ";
    public static final String MSG_SENDING_N = "\nSasa fomu ";
    public static final String MSG_SUCCESS = "Ujumbe umepokelewa!";
    public static final String MSG_FAILED = "Ujumbe haujapokelewa ila umehifadhiwa.";
    public static final String MSG_TOO_LONG = "Ujumbe haujapokelewa ila umehifadhiwa.";
    public static final String MSG_UNKNOWN_ERROR = "Unknown sending error; form not sent!";
    //#else
    public static final String MSG_N_COMP = "Succesfully Transmitted Form ";
    public static final String MSG_SENDING_N = "\nCurrently Sending Form ";
    public static final String MSG_SUCCESS = "Form has been submitted successfully! Your reference is: ";
    public static final String MSG_FAILED = "Submission failed! Please try to submit the form again later in 'View Saved'.";
    public static final String MSG_TOO_LONG = "Sending is taking a long time; you may check on the status and/or resend later in 'View Saved'";
    public static final String MSG_UNKNOWN_ERROR = "Unknown sending error; form not sent!";
    //#endif
        
	public MultiSubmitStatusScreen (CommandListener listener, int[] modelIDs) {
    	//#style submitPopup
		super("Send Status");
		this.modelIDs = modelIDs;
		response = "";
		setCommandListener(listener);
		
		okCommand = new Command("OK", Command.OK, 1);
		msg = new StringItem(null, getCurrentDisplay());

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
		int status = JavaRosaServiceProvider.instance().getTransportManager().getModelDeliveryStatus(modelIDs[currentid], false);
		updateStatus(status);
	}
	
	public void updateStatus(int status) {
			counter += REFRESH_INTERVAL;

			if (!(status == TransportMessage.STATUS_NEW || status == TransportMessage.STATUS_DELIVERED || status == -1)) {
				timer.cancel();
			}

			String message;
			switch (status) {
			case TransportMessage.STATUS_NEW:
			case -1:
				message = (counter < TIMEOUT ? getCurrentDisplay() : MSG_TOO_LONG); break;
			case TransportMessage.STATUS_DELIVERED:
				response += getServerResponse() + "\n";
				currentid++;
				if(currentid == modelIDs.length) {
					message = MSG_SUCCESS  + response;
					timer.cancel();
				} else {
					message = getCurrentDisplay();
				}
				break;
			case TransportMessage.STATUS_FAILED:    
				message = MSG_FAILED; break;
			default:                                
				message = MSG_UNKNOWN_ERROR; break;
			}

			msg.setText(message);
	}
	
	private String getCurrentDisplay() {
		String s = "";
	    //#if commcare.lang.sw
		s = MSG_N_COMP + currentid + " kati ya " + modelIDs.length + MSG_SENDING_N + (currentid+1) + " kati ya " + modelIDs.length + " inaenda";
		//#else
		s = MSG_N_COMP + currentid + " of " + modelIDs.length + MSG_SENDING_N + (currentid+1) + " of " + modelIDs.length;
		//#endif
		return s;
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

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		// TODO Auto-generated method stub
		return this;
	}

}
