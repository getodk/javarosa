/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
