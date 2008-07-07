package org.openmrs.transport.midp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.clforms.ModelList;
import org.javarosa.clforms.TransportShell;
import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.storage.Model;
import org.javarosa.properties.PropertyManager;
import org.openmrs.transport.MessageListener;
import org.openmrs.transport.TransportManager;
import org.openmrs.transport.TransportMessage;
import org.openmrs.transport.TransportMethod;
import org.openmrs.transport.file.FileConnectionTransportMethod;
import org.openmrs.transport.http.HttpTransportMethod;
import org.openmrs.transport.storage.RmsStorage;
import org.openmrs.transport.util.Logger;

/**
 *
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class TransportLayer implements
		CommandListener, MessageListener {

	/**
	 *
	 */
	private TransportManager transportManager;

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

	private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
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

	private TransportShell shell;

	private Model data;

	private int currentMethod;

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {
		System.out.println("destroyApp(" + unconditional + ')');
		if (transportManager != null) {
			transportManager.cleanUp();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {
		System.out.println("pauseApp()");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	public TransportLayer(TransportShell tShell) {
		this.shell = tShell;
		System.out.println("TLAYER startApp()");
		if (transportManager == null) {
			transportManager = new TransportManager(new RmsStorage());
			transportManager.setMessageListener(this);
			transportManager.registerTransportMethod(new HttpTransportMethod());
			//#if app.usefileconnections
			transportManager
			.registerTransportMethod(new FileConnectionTransportMethod());
			//#endif
		}
		String[] elements = {"Select Models", "Send data via HTTP","Save to file system","Message Queue"};
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
		//createView();

	}

	public void createView() {
		Display.getDisplay(shell).setCurrent(mainMenu);
		System.out.println(this.data);
	}

	/**
	 *
	 */
	private void populateMessageList() {
		Enumeration messages = transportManager.getMessages();
		while (messages.hasMoreElements()) {
			TransportMessage message = (TransportMessage) messages
					.nextElement();
			System.out.println(message);
			StringBuffer listEntry = new StringBuffer();
			listEntry.append("ID: ").append(message.getRecordId());
			listEntry.append(" - ").append(new String(message.getReplyloadData()));
			listEntry.append(" - ").append(message.statusToString());
			//System.out.println(listEntry.toString());
			messageList.append(listEntry.toString(), null);
		}
	}

	public Enumeration getTransportMessages(){

		return transportManager.getMessages();
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
					shell.createView();
			} else if (c == CMD_OK) {
				int selected = ((List) mainMenu).getSelectedIndex();
				System.out.println("Selected: " + selected);
				switch (selected) {
				case 0:
					ModelList modelList = new ModelList(this.shell,this);
					break;
				case 1:
					showURLform();
					//display.setCurrent(urlForm);
					break;
			/*	case 2:
					try {
						sendData(TransportMethod.HTTP_GCF);
					} catch (IOException e) {
						Logger.log(e);
					}
					break;*/
				case 2:
					//getDestinationString();
					urlForm = new Form ("FILENAME");
					destinationUrl = shell.getAppProperty("destination-file");
					textField = new TextField("Please enter destination path + filename",
							destinationUrl,140,TextField.ANY);
					/*for (Enumeration en = FileSystemRegistry.listRoots(); en
					.hasMoreElements();) {
						String root = (String) en.nextElement();
						urlForm.append(root);
						Logger.log("Root: " + root);
					}*/
					urlForm.append(textField);
					urlForm.addCommand(CMD_OK);
					urlForm.addCommand(CMD_BACK);
					urlForm.setCommandListener(this);
					this.currentMethod = TransportMethod.FILE;
					Display.getDisplay(shell).setCurrent(urlForm);
					//display.setCurrent(urlForm);
					break;
				/*case 4:
					try {
						sendData(TransportMethod.FILE);
					} catch (IOException e) {
						Logger.log(e);
					}
					break;*/
				case 3:
					showMessageList();
					//display.setCurrent(messageList);
					break;


				}
			} else if (c == CMD_DEBUG) {
				Logger.getInstance().show(Display.getDisplay(shell));
			}
		} else if (d == messageList) {
			if (c == CMD_BACK) {
				this.shell.displayModelList();
			} else if (c == CMD_DETAILS) {
				int selected = messageList.getSelectedIndex();
				Enumeration messages = transportManager.getMessages();
				TransportMessage message = (TransportMessage) elementAt(
						selected, messages);
				if (message != null) {
					messageDetailTextBox.setString(message.toString());
					Display.getDisplay(shell).setCurrent(messageDetailTextBox);
					//display.setCurrent(messageDetailTextBox);
				}
			}
			else if (c == CMD_DELETEMSG) {
				int selected = messageList.getSelectedIndex();
				Enumeration messages = transportManager.getMessages();
				TransportMessage message = (TransportMessage) elementAt(
						selected, messages);

				transportManager.deleteMessage(message.getRecordId());
				showMessageList();
			}
		} else if (d == messageDetailTextBox) {
			if (c == CMD_BACK) {
				showMessageList();
			} else if (c == CMD_SEND) {
				int selected = messageList.getSelectedIndex();
				TransportMessage message = (TransportMessage) elementAt(
						selected, transportManager.getMessages());
				transportManager.send(message, TransportMethod.HTTP_GCF);
			}
		}
		else if (d == urlForm) {
			if (c == CMD_BACK) {
				this.shell.displayModelList();
//				Display.getDisplay(shell).setCurrent(mainMenu);
			} else if (c == CMD_OK) {
				processURLform();
				try {
				if(currentMethod == TransportMethod.HTTP_GCF)
					sendData(TransportMethod.HTTP_GCF);
				else if (currentMethod == TransportMethod.FILE)
					sendData(TransportMethod.FILE);
				} catch (IOException e) {
					Logger.log(e);
				}
				this.shell.displayModelList();
			}

		}
	}

	public void showMessageList() {
		messageList.deleteAll();
		populateMessageList();
		Display.getDisplay(shell).setCurrent(messageList);
	}
	
	public void showURLform() {
		showURLform(this); 
	}

	public void showURLform(CommandListener listener) {
		urlForm = new Form ("URL");
		//destinationUrl = shell.getAppProperty("destination-url");
		destinationUrl = PropertyManager.instance().getSingularProperty("PostURL");
		System.out.println("URL PROP: " + destinationUrl);
		textField = new TextField("Please enter destination string",
				destinationUrl,140,TextField.URL);
		urlForm.append(textField);
		urlForm.addCommand(CMD_OK);
		urlForm.addCommand(CMD_BACK);
		urlForm.setCommandListener(listener);
		this.currentMethod = TransportMethod.HTTP_GCF;
		Display.getDisplay(shell).setCurrent(urlForm);
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
		System.out.println("Destination URL: " + destinationUrl);
		Vector existingURLs = PropertyManager.instance().getProperty(Constants.POST_URL_LIST);
		System.out.println("Existing URLs: " + existingURLs.toString());
		if(!existingURLs.contains(destinationUrl)) {
		    existingURLs.addElement(destinationUrl);
		    PropertyManager.instance().setProperty(Constants.POST_URL_LIST, existingURLs);
		}
		System.out.println("NEw Existing URLs: " + PropertyManager.instance().getProperty(Constants.POST_URL_LIST).toString());

		if(this.data != null){
			System.out.println("WANT TO SEND "+this.data+this.data.getRecordId());
			String testString = this.data.toString();
			transportManager.enqueue(this.data.toString().getBytes("UTF-8"),
					destinationUrl, transportMethod,this.data.getRecordId());
		}else{
			javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("noDataAlert", "No data has been selected",null,
					AlertType.ERROR);
			Display.getDisplay(shell).setCurrent(a,this.mainMenu);
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

		Display.getDisplay(shell).setCurrent(new javax.microedition.lcdui.Alert(title, message, null, type), Display.getDisplay(shell).getCurrent());

	}

	public void setData(Model model) {
		this.data = model;
	}

	public String getDestinationUrl() {
		return destinationUrl;
	}

	public void setDestinationUrl(String destinationUrl) {
		this.destinationUrl = destinationUrl;
	}

}
