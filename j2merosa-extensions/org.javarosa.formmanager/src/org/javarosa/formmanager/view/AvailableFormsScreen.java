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

package org.javarosa.formmanager.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.javarosa.formmanager.activity.DisplayFormsHttpActivity;

public class AvailableFormsScreen extends List{
	
	// TODO: this var is unused
	//private DisplayFormsHttpActivity parentActivity;
	
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);

	public AvailableFormsScreen(String label,String[] elements,DisplayFormsHttpActivity parentActivity) {
		super(label, List.IMPLICIT,elements,null);
		addCommand(CMD_CANCEL);
	//	this.parentActivity = parentActivity;
		setCommandListener(parentActivity);
	}

}
