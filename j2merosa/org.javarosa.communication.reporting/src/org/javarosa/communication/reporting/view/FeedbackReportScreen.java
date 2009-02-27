/**
 * 
 */
package org.javarosa.communication.reporting.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Feb 27, 2009 
 *
 */
public class FeedbackReportScreen extends TextBox implements IView {
	public static final Command SEND_REPORT = new Command("Send Report", 1, Command.OK);
	public static final Command CANCEL = new Command("Cancel", 2, Command.CANCEL);
	

	public FeedbackReportScreen(String title) {
		super("Please enter your feedback report.", "", 500, TextField.ANY);
		this.addCommand(SEND_REPORT);
		this.addCommand(CANCEL);
	}


	public Object getScreenObject() {
		return this;
	}

}
