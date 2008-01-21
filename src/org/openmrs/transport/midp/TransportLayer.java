package org.openmrs.transport.midp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
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

import org.celllife.clforms.ModelList;
import org.celllife.clforms.TransportShell;
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

	private String data;

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
			transportManager
			.registerTransportMethod(new FileConnectionTransportMethod());
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
		createView();
		System.out.println("here1");

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
			listEntry.append(" - ").append(message.statusToString());
			//System.out.println(listEntry.toString());
			messageList.append(listEntry.toString(), null);
		}
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
				try {
					destroyApp(true);
					shell.createView();
				} catch (MIDletStateChangeException e) {
					System.out.println(e);
				}
			} else if (c == CMD_OK) {
				int selected = ((List) mainMenu).getSelectedIndex();
				System.out.println("Selected: " + selected);
				switch (selected) {
				case 0:
					ModelList modelList = new ModelList(this.shell,this);
					break;
				case 1:
					urlForm = new Form ("URL");
					destinationUrl = shell.getAppProperty("destination-url");
					textField = new TextField("Please enter destination string",
							destinationUrl,140,TextField.URL);
					urlForm.append(textField);
					urlForm.addCommand(CMD_OK);
					urlForm.addCommand(CMD_BACK);
					urlForm.setCommandListener(this);
					this.currentMethod = TransportMethod.HTTP_GCF;
					Display.getDisplay(shell).setCurrent(urlForm);
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
					messageList.deleteAll();
					populateMessageList();
					Display.getDisplay(shell).setCurrent(messageList);
					//display.setCurrent(messageList);
					break;
				
				
				}
			} else if (c == CMD_DEBUG) {
				Logger.getInstance().show(Display.getDisplay(shell));
			}
		} else if (d == messageList) {
			if (c == CMD_BACK) {
				Display.getDisplay(shell).setCurrent(mainMenu);
				//display.setCurrent(mainMenu);
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
				messageList.deleteAll();
				populateMessageList();
				Display.getDisplay(shell).setCurrent(messageList);
				//display.setCurrent(messageList);
			}
		} else if (d == messageDetailTextBox) {
			if (c == CMD_BACK) {
				messageList.deleteAll();
				populateMessageList();
				Display.getDisplay(shell).setCurrent(messageList);
				//display.setCurrent(messageList);
			} else if (c == CMD_SEND) {
				int selected = messageList.getSelectedIndex();
				TransportMessage message = (TransportMessage) elementAt(
						selected, transportManager.getMessages());
				transportManager.send(message, TransportMethod.HTTP_GCF);
			}
		}
		else if (d == urlForm) {
			if (c == CMD_BACK) {
				Display.getDisplay(shell).setCurrent(mainMenu);
				//display.setCurrent(mainMenu);
			} else if (c == CMD_OK) {
				destinationUrl = textField.getString();
				try {
				if(currentMethod == TransportMethod.HTTP_GCF)
					sendData(TransportMethod.HTTP_GCF);
				else if (currentMethod == TransportMethod.FILE)
					sendData(TransportMethod.FILE);
				} catch (IOException e) {
					Logger.log(e);
				}
				Display.getDisplay(shell).setCurrent(mainMenu);
				//display.setCurrent(mainMenu);
			}
			
		}
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

	/**
	 * @throws IOException
	 */
	private void sendDataOrig(int transportMethod) throws IOException {
		System.out.println("Destination URL: " + destinationUrl);

		String testData = "TESTDATA007";
		
		InputStream in = TransportLayer.class
				.getResourceAsStream("/testdata.xml");
		System.out.println("in=" + in);
		if (in != null) {
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(in, "UTF-8");
				StringBuffer buffer = new StringBuffer();
				int ch;
				while ((ch = isr.read()) > -1) {
					buffer.append((char) ch);
				}
				testData = buffer.toString();
				System.out.println(testData);
			} catch (Exception e) {
				Logger.log(e);
			}
		} else {
			// use this for testing if data cannot be read from file
			testData = "TESTDATA007";
			transportManager.enqueue(testData.getBytes("UTF-8"),
					destinationUrl, transportMethod);
		}
	}
	
	
	/**
	 * @throws IOException
	 */
	private void sendData(int transportMethod) throws IOException {
		System.out.println("Destination URL: " + destinationUrl);

		if(this.data != null){
			System.out.println("WANT TO SEND "+this.data);
			transportManager.enqueue(this.data.getBytes("UTF-8"),
					destinationUrl, transportMethod);
		}else{
			Alert a = new Alert("noDataAlert", "No data has been selected",null,
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

		Display.getDisplay(shell).setCurrent(new Alert(title, message, null, type), Display.getDisplay(shell).getCurrent());

	}

	public void setData(String string) {
		this.data = string;
	}

}
