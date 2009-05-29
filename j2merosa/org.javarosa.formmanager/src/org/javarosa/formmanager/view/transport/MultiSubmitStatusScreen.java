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
import org.javarosa.formmanager.view.ISubmitStatusScreen;

/**
 * Note: This screen assumes that the model IDs provided will be sent in a more
 * or less sequential fashion.
 * 
 * @author Clayton Sims
 * @date Jan 11, 2009
 * 
 */
public class MultiSubmitStatusScreen extends Form implements
		ISubmitStatusScreen, CommandListener {

	private static final int REFRESH_INTERVAL = 1000;
	private static final int TIMEOUT = 180000;

	private StringItem msg;// displayed
	private Timer timer;

	private int currentid;
	private int counter = 0;
	private int[] modelIDs;
	private int failed = 0;

	private FormTransportActivity activity;

	/**
	 * @param listener
	 * @param modelIDs
	 */
	public MultiSubmitStatusScreen(CommandListener listener) {
		//#style submitPopup
		super(JavaRosaServiceProvider.instance().localize("sending.status.title"));

		setCommandListener(listener);

		addCommand(new Command(JavaRosaServiceProvider.instance().localize("menu.ok"), Command.OK, 1));

		this.activity = (FormTransportActivity) listener;
	}

	public void reinit(int[] ids) {
		deleteAll();
		setModelIDs(ids);
		setMessage();
		addTimerTask();
		failed = 0;
	}

	/**
	 * Error situation - no data to send. when "sent unsent" is called with no forms
	 */
	public void reinitNodata() {
		deleteAll();
		setMessage(JavaRosaServiceProvider.instance().localize("sending.status.none"));
		addTimerTask();
		failed = 0;
	}

	private void setMessage(String s) {
		append(new Spacer(80, 0));

		this.msg = new StringItem(null, s);

		append(this.msg);

	}

	private void setMessage() {
		append(new Spacer(80, 0));
		if (this.modelIDs.length == 0)
			this.msg = new StringItem(null, "No forms to send");
		else
			this.msg = new StringItem(null, getCurrentDisplay());
		append(this.msg);

	}

	public void commandAction(Command c, Displayable d) {

		// only command is ok
		this.activity.returnComplete();
	}

	/**
	 * updates diplay with send status every REFRESH INTERVAL millis
	 */
	private void addTimerTask() {
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
	protected void updateStatus() {
		// get current status
		int status = JavaRosaServiceProvider.instance().getTransportManager()
				.getModelDeliveryStatus(this.modelIDs[this.currentid], false);
		// show it
		updateStatusDisplay(status);
	}

	/**
	 * @param status
	 */
	private void updateStatusDisplay(int status) {

		System.out.println("updateStatusDisplay status= " + status);
		this.counter += REFRESH_INTERVAL;

		// stop the timer if the status is...(TODO: ? explain)
		// Clayton Sims - May 29, 2009 : Seems like it's checking to see whether the 
		// status of the latest message is new (unsent) or delivered (finished) and otherwise
		// it's assuming that something failed, which seem like the wrong semantics for a batch
		// send.
		if (!(status == TransportMessage.STATUS_NEW
				|| status == TransportMessage.STATUS_DELIVERED || status == -1)) {
			this.timer.cancel();
		}

		switch (status) {
		//
		case TransportMessage.STATUS_NEW:
		case -1: {// TODO: what does -1 mean?
			String message = (this.counter < TIMEOUT ? getCurrentDisplay()
					: JavaRosaServiceProvider.instance().localize("sending.status.long"));
			this.msg.setText(message);
			break;
		}
			// finished
		case TransportMessage.STATUS_DELIVERED: {

			this.currentid++;
			if (this.currentid == this.modelIDs.length) {
				// timer already cancelled above
				// this.timer.cancel();
			} else {
				this.msg.setText(getCurrentDisplay());
			}

			break;
		}

			// problem occured
		case TransportMessage.STATUS_FAILED:
			//Move along to the next form.
			this.currentid++;
			failed++;
			//this.msg.setText(JavaRosaServiceProvider.instance().localize("sending.status.failed"));
			break;

		// another problem
		default:
			// #debug error
			System.out.println("Unrecognised status from Transport Manager: "
					+ status);

			this.currentid++;
			failed++;
			this.msg.setText(JavaRosaServiceProvider.instance().localize("sending.status.error"));
			break;
		}
		
		if(this.currentid == this.modelIDs.length) {
			constructFinalMessage();
		}

	}
	
	private void constructFinalMessage() {
		String message = "";
		if(failed > 0) { 
			message += JavaRosaServiceProvider.instance().localize("sending.status.failures", new String[]{String.valueOf(failed)}) + "\n";
		}
		if(failed < this.modelIDs.length) {
			message+= JavaRosaServiceProvider.instance().localize("sending.status.success") + " " + getServerResponse() + "\n";
		}
		if(failed == this.modelIDs.length) {
			message = JavaRosaServiceProvider.instance().localize("sending.status.failed");
		}
		this.msg.setText(message);
	}

	/**
	 * @return
	 */
	private String getCurrentDisplay() {
		return JavaRosaServiceProvider.instance().localize("sending.status.multi", new String[] {
				String.valueOf(currentid), 
				String.valueOf(modelIDs.length),
				String.valueOf(currentid + 1),
				String.valueOf(modelIDs.length)
		});
	}

	/**
	 * @return
	 */
	public String getServerResponse() {
		Enumeration messages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();

		TransportMessage r = (TransportMessage) messages.nextElement();
		return new String(r.getReplyloadData()); // this does not seem
		// terribly robust // TODO: explain?

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.formmanager.view.ISubmitStatusScreen#destroy()
	 */
	public void destroy() {
		this.deleteAll();
		this.timer.cancel();
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.ISubmitStatusScreen#receiveMessage(int)
	 */
	public void receiveMessage(int message, String details) {
		//NOTE: This message comes from the code that handles the overall Transport Manager
		//communication, NOT from the transport method itself. Its failures happen at a high
		//level and shouldn't be confused with errors returned from the transport method.
		switch(message) {
		    default:
		    	//TODO: Specific sending error?
				destroy();
				StringItem failure = new StringItem("","");
		    	failure.setText(JavaRosaServiceProvider.instance().localize("sending.status.error") + ": " + details);
				this.append(failure);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

	public int[] getModelIDs() {
		return modelIDs;
	}

	public void setModelIDs(int[] modelIDs) {
		this.modelIDs = modelIDs;
	}

}
