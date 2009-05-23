package org.javarosa.formmanager.view.transport;

import javax.microedition.lcdui.Command;

import org.javarosa.formmanager.utility.Terms;

public class FormTransportCommands {
	public static final Command CMD_BACK = new Command(Terms.BACK_STR,
			Command.BACK, 1);
	public static final Command CMD_OK = new Command(Terms.OK_STR, Command.OK,
			1);
	public static final Command CMD_DEBUG = new Command(Terms.DEBUG_LOG_STR,
			Command.SCREEN, 2);
	public static final Command CMD_SEND = new Command(Terms.SEND_MESSAGE_STR,
			Command.OK, 1);
	public static final Command CMD_DETAILS = new Command(
			Terms.SHOW_DETAILS_STR, Command.SCREEN, 1);
	public static final Command CMD_SEND_ALL_UNSENT = new Command(
			Terms.SEND_UNSENT_STR, Command.OK, 2);

	public static final Command CMD_DELETEMSG = new Command(
			Terms.DELETE_MESSAGE_STR, Command.SCREEN, 1);

}
