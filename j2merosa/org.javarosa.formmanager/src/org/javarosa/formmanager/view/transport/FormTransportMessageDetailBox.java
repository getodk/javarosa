package org.javarosa.formmanager.view.transport;

import java.util.Enumeration;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.formmanager.activity.FormTransportActivity;
import org.javarosa.formmanager.utility.Terms;

public class FormTransportMessageDetailBox extends TextBox implements
		CommandListener {

	private FormTransportActivity activity;

	public FormTransportMessageDetailBox(CommandListener activity) {

		super(Terms.MESSAGE_DETAILS_STR, null, 250, TextField.UNEDITABLE);
		addCommand(FormTransportCommands.CMD_BACK);
		addCommand(FormTransportCommands.CMD_SEND);
		setCommandListener(activity);
		this.activity = (FormTransportActivity)activity;

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
		if (c == FormTransportCommands.CMD_BACK) {
			this.activity.showMessageList();
		} 

		
		if (c == FormTransportCommands.CMD_SEND) {
			int selected = this.activity.getView().getMessageList().getSelectedIndex();
			TransportMessage message = (TransportMessage) elementAt(selected,
					JavaRosaServiceProvider.instance().getTransportManager()
							.getMessages());
			ITransportManager manager = JavaRosaServiceProvider.instance()
					.getTransportManager();
			JavaRosaServiceProvider.instance().getTransportManager().send(
					message, manager.getCurrentTransportMethod());
		}
		
		throw new RuntimeException("Command " + c.getLabel()
				+ " uncaught - display:" + d.getTitle());
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

}
