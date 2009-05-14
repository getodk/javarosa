package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.formmanager.utility.TransportContext;
import org.javarosa.formmanager.view.ISubmitStatusScreen;
import org.javarosa.formmanager.view.MultiSubmitStatusScreen;
import org.javarosa.formmanager.view.SubmitScreen;
import org.javarosa.formmanager.view.SubmitStatusScreen;

/**
 *
 * TODO: This Activity needs to have most of its functionality delegated out to a controller. Badly.
 * TODO: It also needs a lot of unused variables cleaned up.
 *
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class FormTransportActivity implements
		CommandListener, MessageListener, IActivity, ItemStateListener {

	/**
	 *
	 */
	private List mainMenu;

	/**
	 *
	 */
	private List messageList;

	/**
	 *
	 */
	private TextBox loggingTextBox;

	/**
	 *
	 */
	private TextBox messageDetailTextBox;

	private SubmitScreen submitScreen;

	private ISubmitStatusScreen submitStatusScreen;

	//private Display display;

	public Alert alert;

	int counter;

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 1);
	private static final Command CMD_OK = new Command("OK", Command.OK, 1);
	private static final Command CMD_DEBUG = new Command("Debug log",
			Command.SCREEN, 2);
	private static final Command CMD_SEND = new Command("Send message",
			Command.OK, 1);
	private static final Command CMD_DETAILS = new Command("Show details",
			Command.SCREEN, 1);
	private static final Command CMD_SEND_ALL_UNSENT = new Command("Send all unsent",
			Command.OK, 2);

	private static final Command CMD_DELETEMSG = new Command("Delete message",Command.SCREEN,1);
	
	private ITransportDestination destination;

	private IDataModelSerializingVisitor dataModelSerializer;

	private IShell shell;

	private DataModelTree data;
	
	private Vector multiData;

	private int currentMethod;

	private TransportContext context;

	//private String currentView;

	Vector transportMethods;

	public static final String RETURN_KEY = "returnval";

	public static final String VIEW_MODELS = "viewmodels";
	
	public static final String NEW_DESTINATION = "dest";

	String task;

	/*
	 * (non-Javadoc)
	 *
	 */
	public FormTransportActivity(IShell parentShell) {
		this.shell = parentShell;
	}

	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);
		//TODO: I'm _sure_ that we care about some change here
	}

	public void destroy() {
		// Nothing for this Module
	}

	public void halt() {
		//TODO: setup which view we're on.
		//currentView = TransportContext.MAIN_MENU;
	}

	public void resume(Context globalContext) {
		this.context.mergeInContext(globalContext);
		ITransportDestination destination = this.context.getDestination();
		if(destination != null) {
			setDestination(destination);
		}
		createView(context.getRequestedTask());
	}

	public void start(Context context) {
		this.context = new TransportContext(context);
		//TODO: Parse context arguments, we possibly do not want to create a view
		createView(this.context.getRequestedTask());
	}

	public void createView(String task) {
		this.task = task;
		transportMethods = new Vector();
		Enumeration availableMethods = JavaRosaServiceProvider.instance().getTransportManager().getTransportMethods();
		Vector menuItems = new Vector();
		menuItems.addElement("Select Models");
		menuItems.addElement("Message Queue");
		while(availableMethods.hasMoreElements()) {
			TransportMethod method = (TransportMethod)availableMethods.nextElement();
			menuItems.addElement("Transport with " + method.getName());
			transportMethods.addElement(new Integer(method.getId()));
		}
		String[] elements = new String[menuItems.size()];
		menuItems.copyInto(elements);
		mainMenu = new List("Transport Menu", List.IMPLICIT, elements, null);
		mainMenu.addCommand(CMD_BACK);
		mainMenu.addCommand(CMD_DEBUG);
		mainMenu.setSelectCommand(CMD_OK);
		mainMenu.setCommandListener(this);
		messageList = new List("Messages", List.IMPLICIT);
		messageList.addCommand(CMD_BACK);
		messageList.addCommand(CMD_DETAILS);
		messageList.addCommand(CMD_DELETEMSG);
		messageList.addCommand(CMD_SEND_ALL_UNSENT);
		messageList.setSelectCommand(CMD_DETAILS);
		messageList.setCommandListener(this);
		loggingTextBox = new TextBox("Log messages", null, 1000,
				TextField.UNEDITABLE);
		loggingTextBox.addCommand(CMD_BACK);

		messageDetailTextBox = new TextBox("Message Details", null, 250,
				TextField.UNEDITABLE);
		messageDetailTextBox.addCommand(CMD_BACK);
		messageDetailTextBox.addCommand(CMD_SEND);
		messageDetailTextBox.setCommandListener(this);

		if(task.equals(TransportContext.MAIN_MENU)) {
			shell.setDisplay(this, new IView() {public Object getScreenObject() {return mainMenu;}});
		}
		else if(task.equals(TransportContext.MESSAGE_VIEW)) {
			showMessageList();
		}
		else if(task.equals(TransportContext.SEND_DATA)) {
			multiData = null;
			if(destination == null) {
				submitScreen = new SubmitScreen();
				submitScreen.setCommandListener(this);
				submitScreen.setItemStateListener(this);
				shell.setDisplay(this,submitScreen);
			} else {
				try {
					sendData(JavaRosaServiceProvider.instance().getTransportManager().getCurrentTransportMethod());
				} catch (IOException e) {
					//TODO: Handle this exception reasonably!!!!!
					e.printStackTrace();
				}
			}
		} else if(task.equals(TransportContext.SEND_MULTIPLE_DATA)) {
			multiData = context.getMultipleData();
			if(destination == null) {
				submitScreen = new SubmitScreen();
				submitScreen.setCommandListener(this);
				submitScreen.setItemStateListener(this);
				shell.setDisplay(this,submitScreen);
			} else {
				try {
					sendData(JavaRosaServiceProvider.instance().getTransportManager().getCurrentTransportMethod());
				} catch (IOException e) {
					//TODO: Handle this exception reasonably!!!!!
					e.printStackTrace();
				}
			}
		}
		}

	/**
	 *
	 */
	private void populateMessageList() {
		Enumeration messages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
		while (messages.hasMoreElements()) {
			TransportMessage message = (TransportMessage) messages
					.nextElement();
			//#if debug.output==verbose
			System.out.println(message);
			//#endif
			StringBuffer listEntry = new StringBuffer();
			listEntry.append("ID: ").append(message.getRecordId());
			listEntry.append(" - ").append(message.statusToString());
			messageList.append(listEntry.toString(), null);
		}
	}

	public Enumeration getTransportMessages(){
		return JavaRosaServiceProvider.instance().getTransportManager().getMessages();
	}

	/**
	 * @return the dataModelSerializer
	 */
	public IDataModelSerializingVisitor getDataModelSerializer() {
		return dataModelSerializer;
	}

	/**
	 * @param dataModelSerializer the dataModelSerializer to set
	 */
	public void setDataModelSerializer(
			IDataModelSerializingVisitor dataModelSerializer) {
		this.dataModelSerializer = dataModelSerializer;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.ItemStateListener#itemStateChanged(javax.microedition.lcdui.Item)
	 */
	public void itemStateChanged(Item arg0) {
		//This is for the submit Screen
			switch (submitScreen.getCommandChoice()) {
			case SubmitScreen.SEND_NOW_DEFAULT:
				ITransportManager tmanager = JavaRosaServiceProvider.instance().getTransportManager();
				currentMethod = tmanager.getCurrentTransportMethod();
				ITransportDestination destination = tmanager.getDefaultTransportDestination(currentMethod);
				if (destination != null) {
					this.setDestination(destination);
					try {
						sendData(currentMethod);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					returnForDestination();
				}
				break;
			case SubmitScreen.SEND_NOW_SPEC:
				returnForDestination();
				break;
			case SubmitScreen.SEND_LATER:
				shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
				break;
			}

	}
	
	/**
	 * This method returns to the shell requesting a new Transport Destination
	 */
	private void returnForDestination() {
		Hashtable returnArgs = new Hashtable();
		returnArgs.put(RETURN_KEY, NEW_DESTINATION);
		shell.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION, returnArgs);
	}
	

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command,
	 *      javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) {
		if (d == mainMenu) {
			if (c == CMD_BACK) {
					shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
			} else if (c == CMD_OK) {
				int selected = ((List) mainMenu).getSelectedIndex();

				switch (selected) {
				case 0:
					Hashtable returnArgs = new Hashtable();
					returnArgs.put(RETURN_KEY, VIEW_MODELS);
					shell.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION, returnArgs);
					break;
				case 1:
					returnForDestination();
					showMessageList();
					break;
				default:
					//1-10-2009 - ctsims
					//The comment below sketches me out. That doesn't seem like an acceptable way to 
					//index menu items.
					
					//Offset from always-present menu choices
					int transportType = selected - 2;

				    Integer id = (Integer)transportMethods.elementAt(transportType);
				    TransportMethod method = JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(id.intValue());
				    if (method.getId() == TransportMethod.FILE) {
				    	this.returnForDestination();
					}
					break;
				case 3:
					break;
				}
			}
		} else if (d == messageList) {
			if (c == CMD_BACK) {
				if(this.task == TransportContext.MESSAGE_VIEW) {
					shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
				}
				else {
					shell.setDisplay(this, new IView() {public Object getScreenObject() {return mainMenu;}});
				}
			} else if (c == CMD_DETAILS) {
				int selected = messageList.getSelectedIndex();
				Enumeration messages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
				TransportMessage message = (TransportMessage) elementAt(
						selected, messages);
				if (message != null) {
					messageDetailTextBox.setString(message.toString());
					shell.setDisplay(this, new IView() {public Object getScreenObject() {return messageDetailTextBox;}});
				}
			}
			else if (c == CMD_DELETEMSG) {
				int selected = messageList.getSelectedIndex();
				Enumeration messages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
				TransportMessage message = (TransportMessage) elementAt(
						selected, messages);

				JavaRosaServiceProvider.instance().getTransportManager().deleteMessage(message.getRecordId());
				showMessageList();
			}
		} else if (d == messageDetailTextBox) {
			if (c == CMD_BACK) {
				showMessageList();
			} else if (c == CMD_SEND) {
				int selected = messageList.getSelectedIndex();
				TransportMessage message = (TransportMessage) elementAt(
						selected, JavaRosaServiceProvider.instance()
								.getTransportManager().getMessages());
				ITransportManager manager = JavaRosaServiceProvider.instance().getTransportManager();
				JavaRosaServiceProvider.instance().getTransportManager().send(
						message, manager.getCurrentTransportMethod());
			}
		}  else if (d == submitStatusScreen) {
			submitStatusScreen.destroy();
			submitStatusScreen = null;
			shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
		}
	}

	public void showMessageList() {
		messageList.deleteAll();
		populateMessageList();
		shell.setDisplay(this, new IView() {public Object getScreenObject() {return messageList;}});
	}

	private Object elementAt(int index, Enumeration en) {
		int i = 0;
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			if (i == index) {
				return o;
			}
			i++;
		}
		return null;
	}

	public void setDestination(ITransportDestination destination) {
		this.destination = destination;
	}

	/**
	 * @throws IOException
	 */
	public void sendData(int transportMethod) throws IOException {
		if (this.multiData != null) {
			int[] ids = new int[multiData.size()];
			
			for(int i = 0; i < ids.length; ++i) {
				ids[i] = ((IDataPayload)multiData.elementAt(i)).getTransportId();
			}
			
			submitStatusScreen = new MultiSubmitStatusScreen(this, ids);
			shell.setDisplay(this, submitStatusScreen);
			
			for(Enumeration en = multiData.elements();en.hasMoreElements();) {
				IDataPayload payload = (IDataPayload)en.nextElement();
				JavaRosaServiceProvider.instance().getTransportManager().enqueue(payload,
						destination, transportMethod, payload.getTransportId());
			}
			
		} else {
			if (this.data != null) {
				IDataPayload payload = dataModelSerializer.createSerializedPayload(data);
				JavaRosaServiceProvider.instance().getTransportManager()
						.enqueue(payload, destination, transportMethod,
								this.data.getId());
			} else {
				//javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert(
				//		"noDataAlert", "No data has been selected", null,
				//		AlertType.ERROR);
				shell.setDisplay(this, new IView() {
					public Object getScreenObject() {
						return mainMenu;
					}
				});
			}

			submitStatusScreen = new SubmitStatusScreen(this, data.getId());
			shell.setDisplay(this, submitStatusScreen);
		}
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
		final Alert alert =  new Alert(title, message, null, type);
		shell.setDisplay(this,new IView() {public Object getScreenObject() {return alert;}});

	}

	public void setData(DataModelTree data) {
		this.data = data;
	}

	//this method is unused and almost identical to ModelListActivity.getModelDeliveryStatus
	public int getSubmitStatus(int recordId) {

    	Enumeration qMessages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
    	TransportMessage message;
    	while(qMessages.hasMoreElements()) {
    		message = (TransportMessage) qMessages.nextElement();
    		if(message.getModelId() == recordId)
    			return message.getStatus();
    	}
    	return -1;
	}
	public Context getActivityContext() {
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
