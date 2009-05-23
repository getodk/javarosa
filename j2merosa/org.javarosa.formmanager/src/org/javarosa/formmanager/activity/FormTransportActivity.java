package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.formmanager.utility.FormSender;
import org.javarosa.formmanager.utility.TransportContext;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.SendNowSendLaterForm;

/**
 * Manages the marshalling and sending of XForms through the transport layer
 * 
 * called after completion of PatientEntry activity, for example
 * 
 */
public class FormTransportActivity implements CommandListener, MessageListener,
		IActivity, ItemStateListener {

	/**
	 * calling shell
	 */
	private IShell parent;

	/**
	 * A collection of views
	 */
	private FormTransportViews views;

	/**
	 * utiliy managing the despatching of forms
	 */
	private FormSender sender;

	private TransportContext context;

	public static final String RETURN_KEY = "returnval";

	public static final String VIEW_MODELS = "viewmodels";

	public static final String NEW_DESTINATION = "dest";

	/*
	 * (non-Javadoc)
	 */
	public FormTransportActivity(IShell parentShell) {
		this.parent = parentShell;
		this.sender = new FormSender(this.parent, this);
		this.views = new FormTransportViews(this);

	}

	public void start(Context context) {
		this.context = new TransportContext(context);
		// TODO: Parse context arguments, we possibly do not want to create a
		// view
		reinit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 * 
	 * 
	 * resume will be called ONLY after the user has been prompted to specify a
	 * destination
	 * 
	 * @see returnForDestination
	 */
	public void resume(Context globalContext) {
		this.context.mergeInContext(globalContext);
		ITransportDestination destination = this.context.getDestination();
		if (destination != null) {
			this.sender.setDestination(destination);
		}
		reinit();
	}

	/**
	 * reinit on start and on resume (TODO: is recreating the views necessary?)
	 */
	public void reinit() {
		// this.views = new FormTransportViews(this);
		executeRequestedTask(this.context.getRequestedTask());
	}

	/**
	 * This method returns to the shell requesting a new Transport Destination
	 * 
	 * which is then inserted into context?
	 */
	public void returnForDestination() {
		Hashtable returnArgs = new Hashtable();
		returnArgs.put(RETURN_KEY, NEW_DESTINATION);
		this.parent.returnFromActivity(this,
				Constants.ACTIVITY_NEEDS_RESOLUTION, returnArgs);
	}

	public void returnComplete() {
		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
	}

	public void returnViewModels() {
		Hashtable returnArgs = new Hashtable();
		returnArgs.put(FormTransportActivity.RETURN_KEY,
				FormTransportActivity.VIEW_MODELS);
		this.parent.returnFromActivity(this,
				Constants.ACTIVITY_NEEDS_RESOLUTION, returnArgs);
	}

	public void handleBackFromMessageList() {

		if (this.context.getRequestedTask() == TransportContext.MESSAGE_VIEW) {
			this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					null);
		} else {
			this.parent.setDisplay(this, new IView() {
				public Object getScreenObject() {
					return views.getMainMenu();
				}
			});
		}

	}

	// ------------------------- setting display
	/**
	 * task specified by this.context.getRequestedTask(); TODO: where is the
	 * task set?
	 */
	private void executeRequestedTask(String task) {

		// #debug debug
		System.out.println("task from context: " + task);
		if (task.equals(TransportContext.MAIN_MENU))
			setMainMenuDisplay();

		if (task.equals(TransportContext.MESSAGE_VIEW))
			showMessageList();

		if (task.equals(TransportContext.SEND_DATA))
			sendData(false);

		if (task.equals(TransportContext.SEND_MULTIPLE_DATA))
			sendData(true);

	}

	/**
	 * 
	 */
	private void setMainMenuDisplay() {
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return views.getMainMenu();
			}
		});
	}

	/**
	 * @param multiple
	 */
	private void sendData(boolean multiple) {

		// set properties of the sender
		this.sender.setMultiple(multiple);
		if (multiple)
			this.sender.setMultiData(this.context.getMultipleData());

		if (this.sender.getDestination() == null) {
			this.parent
					.setDisplay(this, this.views.getSendNowSendLaterScreen());
		} else {
			try {

				// send the data
				this.sender.sendData();
			} catch (IOException e) {
				// TODO: Handle this exception reasonably!!!!!
				e.printStackTrace();
				// #debug error
				System.out.println(e.getMessage());
			}
		}
	}

	// ----------------------------------------------------------------------

	/**
	 * @param dataModelSerializer
	 *            the dataModelSerializer to set
	 */
	public void setDataModelSerializer(
			IDataModelSerializingVisitor dataModelSerializer) {
		this.sender.setSerializer(dataModelSerializer);
	}

	public void setData(DataModelTree data) {
		this.sender.setData(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.microedition.lcdui.ItemStateListener#itemStateChanged(javax.
	 * microedition.lcdui.Item)
	 */
	public void itemStateChanged(Item arg0) {
		// This is for the submit Screen
		switch (this.views.getSendNowSendLaterScreen().getCommandChoice()) {
		case SendNowSendLaterForm.SEND_NOW_DEFAULT:

			// #debug debug
			System.out.println("Send NOW selected");
			this.sender.setDefaultDestination();

			if (this.sender.getDestination() != null) {

				try {
					this.sender.sendData();

				} catch (IOException e) {
					// #debug error
					System.out.println("UNABLE TO SEND: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				returnForDestination();
			}
			return;

		case SendNowSendLaterForm.SEND_LATER:

			// #debug debug
			System.out.println("Send LATER selected");
			this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					null);
			return;

			// should be visible only to admin
		case SendNowSendLaterForm.SEND_NOW_SPEC:
			returnForDestination();
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.microedition.lcdui.CommandListener#commandAction(javax.microedition
	 * .lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) {
		// #debug debug
		System.out.println("command: " + c.getLabel() + " d: " + d.getTitle());

		if (d instanceof CommandListener) {
			((CommandListener) d).commandAction(c, d);
			return;
		}

		throw new RuntimeException("Command " + c.getLabel()
				+ " uncaught - display:" + d.getTitle());
	}

	/**
	 * 
	 * From mssage list
	 * 
	 * @param message
	 */
	public void handleTransportMessage(TransportMessage message) {
		this.views.getMessageDetailTextBox().setString(message.toString());
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return views.getMessageDetailTextBox();
			}
		});
	}

	/**
	 * 
	 */
	public void showMessageList() {
		this.views.getMessageList().refresh();
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return views.getMessageList();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.MessageListener#onMessage(java.lang.String)
	 */
	public void onMessage(String message, int messageType) {
		AlertType type;
		String title;
		switch (messageType) {
		case MessageListener.TYPE_INFO:
			type = AlertType.INFO;
			title = "Info";
			break;
		case MessageListener.TYPE_WARNING:
			type = AlertType.WARNING;
			title = "Warning";
			break;
		case MessageListener.TYPE_ERROR:
			type = AlertType.ERROR;
			title = "Error";
			break;
		default:
			type = AlertType.INFO;
			title = "Info";
			break;
		}
		final Alert alert = new Alert(title, message, null, type);
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return alert;
			}
		});

	}

	public Context getActivityContext() {
		return context;
	}

	public void setShell(IShell shell) {
		this.parent = shell;
	}

	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);
		// TODO: I'm _sure_ that we care about some change here
	}

	public void destroy() {
		// Nothing for this Module
	}

	public void halt() {
		// TODO: setup which view we're on.
		// currentView = TransportContext.MAIN_MENU;
	}

	public FormTransportViews getView() {
		return views;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api
	 * .ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException(
				"The Activity Class "
						+ this.getClass().getName()
						+ " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
