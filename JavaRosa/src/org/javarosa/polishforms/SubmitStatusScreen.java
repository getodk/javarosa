package org.javarosa.polishforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import java.util.Timer;
import java.util.TimerTask;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.storage.ModelMetaData;
import org.openmrs.transport.TransportMessage;

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.Spacer;

public class SubmitStatusScreen extends de.enough.polish.ui.Form implements CommandListener {
	private Controller controller;
    private StringItem msg;
	private ModelMetaData mmd;
    private Command okCommand;
    private Timer timer;
    int counter = 0;
    
    private final int REFRESH_INTERVAL = 1000;
    private final int TIMEOUT = 60000;
    
	public SubmitStatusScreen (Controller controller, ModelMetaData mmd, Style style) {
		super("Send Status", style);
		this.controller = controller;
		this.mmd = mmd;
		setCommandListener(this);
		
		okCommand = new Command("OK", Command.OK, 1);
		msg = new StringItem(null, "Sending...");
		
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

	public SubmitStatusScreen (Controller controller, ModelMetaData mmd) {
		this(controller, mmd, null);
	}

	public void updateStatus () {
		counter += REFRESH_INTERVAL;
		
		int status = controller.getSubmitStatus(mmd);
		String message;
		
		System.out.println("checking status: " + counter);
		if (status != TransportMessage.STATUS_NEW)
			timer.cancel();
		
		switch (status) {
		case TransportMessage.STATUS_NEW:       message = (counter < TIMEOUT ? "Sending..."
				: "Sending is taking a long time; you may check on the status and/or resend later in 'View Saved'"); break;
		case TransportMessage.STATUS_DELIVERED: message = "Form has been submitted successfully! " +
				"The Ministry of Health thanks you for your participation in the GATHER field test!"; break;
		case TransportMessage.STATUS_FAILED:    message = "Submission failed! Please try to submit the form again later in 'View Saved'."; break;
		default:                                message = "Unknown sending error; form not sent!"; break;
		}

		msg.setText(message);
	}
	
	public void commandAction (Command c, Displayable d) {
		if (c == okCommand) {
			timer.cancel();
			controller.closeForm();
		}
	}
}
