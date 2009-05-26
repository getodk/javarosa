package org.javarosa.formmanager.view.transport;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.formmanager.activity.FormTransportActivity;
import org.javarosa.formmanager.utility.Terms;
import org.javarosa.formmanager.view.ISubmitStatusScreen;

public class FormTransportSubmitStatusScreen extends Form implements
		ISubmitStatusScreen, CommandListener {
	private int modelID = -1;
	private StringItem msg;
	private Command okCommand;
	private Timer timer;
	private int counter = 0;

	private FormTransportActivity activity;

	private static final int REFRESH_INTERVAL = 1000;
	private static final int TIMEOUT = 60000;

	public FormTransportSubmitStatusScreen(CommandListener activity) {
		//#style submitPopup
		super(Terms.SEND_STATUS);
		setCommandListener(this);

		
		this.activity = (FormTransportActivity) activity;
	}

	
	public void reinit(int modelId){
		
		setModelID(modelId);
		this.okCommand = new Command(Terms.OK_STR, Command.OK, 1);
		this.msg = new StringItem(null, Terms.MSG_SENDING);

		addCommand(this.okCommand);
		append(new Spacer(80, 0));
		append(this.msg);

		initTimer();
	}
	
	
	public void commandAction(Command c, Displayable d) {
		//destroy status screen was here
		this.activity.returnComplete();
	}

	private void initTimer() {
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
			public void run() {
				updateStatus();
			}
		}, REFRESH_INTERVAL, REFRESH_INTERVAL);
	}

	/**
	 * 
	 */
	public void updateStatus() {
		int status = JavaRosaServiceProvider.instance().getTransportManager()
				.getModelDeliveryStatus(this.modelID, false);
		updateStatus(status);
	}

	/**
	 * @param status
	 */
	public void updateStatus(int status) {
		this.counter += REFRESH_INTERVAL;

		if (status != TransportMessage.STATUS_NEW)
			this.timer.cancel();

		String message;
		switch (status) {
		case TransportMessage.STATUS_NEW:
			message = (this.counter < TIMEOUT ? Terms.MSG_SENDING
					: Terms.MSG_TOO_LONG);
			break;
		case TransportMessage.STATUS_DELIVERED:
			message = Terms.MSG_SUCCESS + "  " + getServerResponse();
			break;
		case TransportMessage.STATUS_FAILED:
			message = Terms.MSG_FAILED;
			break;
		default:
			message = Terms.MSG_UNKNOWN_ERROR;
			break;
		}

		this.msg.setText(message);
	}

	public void destroy() {
		deleteAll();
		this.timer.cancel();
	}

	/**
	 * @return
	 */
	public String getServerResponse() {
		Enumeration messages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();
		String receipt = "";

		TransportMessage response = (TransportMessage) messages.nextElement();
		receipt = new String(response.getReplyloadData()); // this does not seem
		// terribly robust

		return receipt;
	}

	public Object getScreenObject() {
		return this;
	}

	public void setModelID(int modelID) {
		this.modelID = modelID;
	}

}
