///*
// * Copyright (C) 2009 JavaRosa
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//
//package org.javarosa.formmanager.view.transport;
//
//import java.util.Enumeration;
//
//import javax.microedition.lcdui.Command;
//import javax.microedition.lcdui.CommandListener;
//import javax.microedition.lcdui.Displayable;
//import javax.microedition.lcdui.TextBox;
//import javax.microedition.lcdui.TextField;
//
//import org.javarosa.core.JavaRosaServiceProvider;
//import org.javarosa.core.services.ITransportManager;
//import org.javarosa.core.services.locale.Localization;
//import org.javarosa.core.services.transport.TransportMessage;
//import org.javarosa.formmanager.activity.FormTransportActivity;
//
//public class FormTransportMessageDetailBox extends TextBox implements
//		CommandListener {
//
//	public FormTransportMessageDetailBox(CommandListener activity) {
//
//		super(Localization.get("sending.message.details"), null, 250, TextField.UNEDITABLE);
//		addCommand(FormTransportCommands.CMD_BACK);
//		addCommand(FormTransportCommands.CMD_SEND);
//		setCommandListener(activity);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * javax.microedition.lcdui.CommandListener#commandAction(javax.microedition
//	 * .lcdui.Command, javax.microedition.lcdui.Displayable)
//	 */
//	public void commandAction(Command c, Displayable d) {
//
//		// #debug debug
//		System.out.println("command: " + c.getLabel() + " d: " + d.getTitle());
//		if (c == FormTransportCommands.CMD_BACK) {
//			this.activity.showMessageList();
//		} 
//
//		
//		if (c == FormTransportCommands.CMD_SEND) {
//			int selected = this.activity.getView().getMessageList().getSelectedIndex();
//			TransportMessage message = (TransportMessage) elementAt(selected,
//					JavaRosaServiceProvider.instance().getTransportManager()
//							.getMessages());
//			ITransportManager manager = JavaRosaServiceProvider.instance()
//					.getTransportManager();
//			JavaRosaServiceProvider.instance().getTransportManager().send(
//					message, manager.getCurrentTransportMethod());
//		}
//		
//		throw new RuntimeException("Command " + c.getLabel()
//				+ " uncaught - display:" + d.getTitle());
//	}
//	
//
//	private Object elementAt(int index, Enumeration en) {
//		int i = 0;
//		while (en.hasMoreElements()) {
//			Object o = en.nextElement();
//			if (i == index) {
//				return o;
//			}
//			i++;
//		}
//		return null;
//	}
//
//}
