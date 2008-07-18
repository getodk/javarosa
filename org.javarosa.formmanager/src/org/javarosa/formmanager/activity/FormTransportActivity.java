package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormData;
import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.formmanager.utility.TransportContext;

/**
 *
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class FormTransportActivity implements
		CommandListener, MessageListener, IActivity {

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

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 1);
	private static final Command CMD_OK = new Command("OK", Command.OK, 1);
	private static final Command CMD_DEBUG = new Command("Debug log",
			Command.SCREEN, 2);
	private static final Command CMD_SEND = new Command("Send message",
			Command.OK, 1);
	private static final Command CMD_DETAILS = new Command("Show details",
			Command.SCREEN, 1);

	private static final Command CMD_DELETEMSG = new Command("Delete message",Command.SCREEN,1);

	/**
	 *
	 */
	private Form urlForm;

	private String destinationUrl;

	private TextField textField;

	private IShell shell;

	private FormData data;

	private int currentMethod;
	
	private TransportContext context;
	
	private String currentView;
	
	Vector transportMethods;
	
	public static final String RETURN_KEY = "returnval";
	
	public static final String VIEW_MODELS = "viewmodels";
	
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
		currentView = TransportContext.MAIN_MENU;
	}

	public void resume(Context globalContext) {
		createView(currentView);		
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
			shell.setDisplay(this, mainMenu);
		}
		else if(task.equals(TransportContext.MESSAGE_VIEW)) {
			shell.setDisplay(this, messageList);
		}
		else if(task.equals(TransportContext.SEND_DATA)) {
			showURLform();
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
					showURLform();
					showMessageList();
					break;
				default:
					//Offset from always-present menu choices
					int transportType = selected - 2;
				    
				    Integer id = (Integer)transportMethods.elementAt(transportType);
				    TransportMethod method = JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(id.intValue());
				    if (method.getId() == TransportMethod.FILE) {
						urlForm = new Form("FILENAME");
						// destinationUrl = shell.getAppProperty("destination-file");
						// TODO: put this back in when the property manager is
						// complete again
						textField = new TextField(
								"Please enter destination path + filename",
								destinationUrl, 140, TextField.ANY);
						
						urlForm.append(textField);
						urlForm.addCommand(CMD_OK);
						urlForm.addCommand(CMD_BACK);
						urlForm.setCommandListener(this);
						this.currentMethod = TransportMethod.FILE;
						shell.setDisplay(this, urlForm);
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
					shell.setDisplay(this, mainMenu);
				}
			} else if (c == CMD_DETAILS) {
				int selected = messageList.getSelectedIndex();
				Enumeration messages = JavaRosaServiceProvider.instance().getTransportManager().getMessages();
				TransportMessage message = (TransportMessage) elementAt(
						selected, messages);
				if (message != null) {
					messageDetailTextBox.setString(message.toString());
					shell.setDisplay(this, messageDetailTextBox);
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
						selected, JavaRosaServiceProvider.instance().getTransportManager().getMessages());
				JavaRosaServiceProvider.instance().getTransportManager().send(message, TransportMethod.HTTP_GCF);
			}
		}
		else if (d == urlForm) {
			if (c == CMD_BACK) {
					shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
			} else if (c == CMD_OK) {
				processURLform();
				try {
				if(currentMethod == TransportMethod.HTTP_GCF)
					sendData(TransportMethod.HTTP_GCF);
				else if (currentMethod == TransportMethod.FILE)
					sendData(TransportMethod.FILE);
				} catch (IOException e) {
					e.printStackTrace();
				}
				shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
			}
		}
	}

	public void showMessageList() {
		messageList.deleteAll();
		populateMessageList();
		shell.setDisplay(this, messageList);
	}
	
	public void showURLform() {
		showURLform(this); 
	}

	public void showURLform(CommandListener listener) {
		urlForm = new Form ("URL");
		destinationUrl = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty("PostURL");
		//TODO: Put this back in once we 
		textField = new TextField("Please enter destination string",
				destinationUrl,140,TextField.URL);
		urlForm.append(textField);
		urlForm.addCommand(CMD_OK);
		urlForm.addCommand(CMD_BACK);
		urlForm.setCommandListener(listener);
		this.currentMethod = TransportMethod.HTTP_GCF;
		shell.setDisplay(this, urlForm);
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
	
	public void processURLform() {
		destinationUrl = textField.getString();
	}

	public void setDestURL (String URL) {
		destinationUrl = URL;
	}
	
	/**
	 * @throws IOException
	 */
	public void sendData(int transportMethod) throws IOException {
		Vector existingURLs = JavaRosaServiceProvider.instance()
				.getPropertyManager().getProperty(
						HttpTransportProperties.POST_URL_LIST_PROPERTY);
		if (!existingURLs.contains(destinationUrl)) {
			existingURLs.addElement(destinationUrl);
			JavaRosaServiceProvider.instance().getPropertyManager()
					.setProperty(
							HttpTransportProperties.POST_URL_LIST_PROPERTY,
							existingURLs);
		}

		if(this.data != null) {
			JavaRosaServiceProvider.instance().getTransportManager().enqueue(this.data.toString().getBytes("UTF-8"),
					destinationUrl, transportMethod,this.data.getRecordId());
		}else{
			javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("noDataAlert", "No data has been selected",null,
					AlertType.ERROR);
			shell.setDisplay(this, this.mainMenu);
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

		shell.setDisplay(this, new javax.microedition.lcdui.Alert(title, message, null, type));

	}

	public void setData(FormData data) {
		this.data = data;
	}

}
