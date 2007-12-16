package org.openmrs.transport.util;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * A simple logger. Useful for debugging on real devices.
 */
public class Logger implements CommandListener {

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 1);
	private static final Command CMD_CLEAR = new Command("Clear",
			Command.SCREEN, 1);

	/**
	 * 
	 */
	private static Logger instance = new Logger();

	/**
	 * 
	 */
	private TextBox loggingTextBox;

	/**
	 * 
	 */
	private Display display;

	/**
	 * 
	 */
	private Displayable previous;

	/**
	 * 
	 */
	private Vector logMessages;

	/**
	 * 
	 */

	/**
	 * Creates a new instance of <code>Logger</code>
	 */
	private Logger() {
		loggingTextBox = new TextBox("Debug messages", "", 500,
				TextField.UNEDITABLE);
		loggingTextBox.addCommand(CMD_BACK);
		loggingTextBox.addCommand(CMD_CLEAR);
		loggingTextBox.setCommandListener(this);
		logMessages = new Vector();
	}

	/**
	 * @return
	 */
	public static Logger getInstance() {
		return instance;
	}

	/**
	 * @param o
	 */
	private void doLog(Object o) {
		System.out.println(o.toString());
		if (o instanceof Exception) {
			((Exception) o).printStackTrace();
		}
		logMessages.addElement(o.toString());
	}

	/**
	 * @param o
	 */
	public static synchronized void log(Object o) {
		getInstance().doLog(o);
	}

	/**
	 * @param display
	 */
	public void show(Display display) {
		this.display = display;
		this.previous = display.getCurrent();
		int maxSize = loggingTextBox.getMaxSize();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < logMessages.size(); i++) {
			String msg = (String) logMessages.elementAt(i);
			if (sb.length() + msg.length() > maxSize) {
				break;
			}
			sb.append(i + 1).append(": ").append(msg).append("\r\n");
		}
		loggingTextBox.setString(sb.toString());
		display.setCurrent(loggingTextBox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command,
	 *      javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) {
		if (c == CMD_BACK) {
			display.setCurrent(previous);
		} else if (c == CMD_CLEAR) {
			logMessages.removeAllElements();
			loggingTextBox.setString("");
		}
	}
}
