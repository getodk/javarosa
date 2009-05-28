package org.javarosa.formmanager.view.transport;

import javax.microedition.lcdui.Command;

import org.javarosa.core.JavaRosaServiceProvider;

public class FormTransportCommands {
	public static final Command CMD_BACK = new Command(JavaRosaServiceProvider.instance().localize("menu.Back"),
			Command.BACK, 1);
	public static final Command CMD_OK = new Command(JavaRosaServiceProvider.instance().localize("menu.ok"), Command.OK,
			1);
	public static final Command CMD_DEBUG = new Command(JavaRosaServiceProvider.instance().localize("debug.log"),
			Command.SCREEN, 2);
	public static final Command CMD_SEND = new Command(JavaRosaServiceProvider.instance().localize("sending.message.send"),
			Command.OK, 1);
	public static final Command CMD_DETAILS = new Command(
			JavaRosaServiceProvider.instance().localize("message.details"), Command.SCREEN, 1);
	public static final Command CMD_SEND_ALL_UNSENT = new Command(
			JavaRosaServiceProvider.instance().localize("message.send.unsent"), Command.OK, 2);

	public static final Command CMD_DELETEMSG = new Command(
			JavaRosaServiceProvider.instance().localize("message.delete"), Command.SCREEN, 1);

}
