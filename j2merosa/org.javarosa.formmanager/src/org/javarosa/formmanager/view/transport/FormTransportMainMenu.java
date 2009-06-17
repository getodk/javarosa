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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.formmanager.activity.FormTransportActivity;

public class FormTransportMainMenu extends List implements CommandListener {

	private FormTransportActivity activity;
	private final static int SELECT_MODELS = 0;
	private final static int MESSAGE_QUEUE = 1;

	public FormTransportMainMenu(CommandListener activity, String title,
			int listType, String[] stringElements, Image[] imageElements) {
		super(title, listType, stringElements, imageElements);

		addCommand(FormTransportCommands.CMD_BACK);
		addCommand(FormTransportCommands.CMD_DEBUG);
		setSelectCommand(FormTransportCommands.CMD_OK);
		setCommandListener(activity);

		this.activity = (FormTransportActivity) activity;

	}

	public static Vector getMenuItems() {
		Vector menuItems = new Vector();
		menuItems.addElement("Select Models");
		menuItems.addElement("Message Queue");

		Enumeration availableMethods = JavaRosaServiceProvider.instance()
				.getTransportManager().getTransportMethods();
		while (availableMethods.hasMoreElements()) {
			TransportMethod method = (TransportMethod) availableMethods
					.nextElement();
			menuItems.addElement("Transport with " + method.getName());
		}

		return menuItems;
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
			this.activity.returnComplete();

		}

		if (c == FormTransportCommands.CMD_OK) {
			int selected = getSelectedIndex();
			handleOk(selected);
		}
		
		throw new RuntimeException("Command " + c.getLabel()
				+ " uncaught - display:" + d.getTitle());
	}

	private void handleOk(int selection) {

		switch (selection) {
		
		case SELECT_MODELS:
			this.activity.returnViewModels();
			return;
			
		case MESSAGE_QUEUE:
			this.activity.returnForDestination();
			this.activity.showMessageList();
			return;
			
			// otherwise..
		default:
			// 1-10-2009 - ctsims
			// The comment below sketches me out. That doesn't seem like
			// an acceptable way to
			// index menu items.

			// Offset from always-present menu choices
			int transportType = selection - 2;

			Integer id = (Integer) getTransportMethods()
					.elementAt(transportType);
			TransportMethod method = JavaRosaServiceProvider.instance()
					.getTransportManager().getTransportMethod(id.intValue());
			
			if (method.getId() == TransportMethod.FILE) {
				this.activity.returnForDestination();
			}
			return;

		}
	}
	
	/**
	 * @return
	 */
	private Vector getTransportMethods() {
		Vector v = new Vector();
		Enumeration availableMethods = JavaRosaServiceProvider.instance()
				.getTransportManager().getTransportMethods();
		while (availableMethods.hasMoreElements()) {
			TransportMethod method = (TransportMethod) availableMethods
					.nextElement();
			v.addElement(new Integer(method.getId()));
		}
		return v;
	}

}
