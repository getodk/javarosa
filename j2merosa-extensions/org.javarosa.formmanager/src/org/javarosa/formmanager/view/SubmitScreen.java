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

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;

import org.javarosa.core.api.IView;
import org.javarosa.core.services.locale.Localization;


public class SubmitScreen extends Form implements IView {
	private ChoiceGroup cg;	
	public static final int SEND_NOW_DEFAULT = 0;
	public static final int SEND_LATER = 1;
	public static final int SEND_NOW_SPEC = 2;
	
	private static final String SEND_NOW_DEFAULT_STRING = Localization.get("menu.SendNow");
	private static final String SEND_NOW_SPEC_STRING = Localization.get("menu.SendToNewServer");
	private static final String SEND_LATER_STRING = Localization.get("menu.SendLater");
	
    //private Command selectCommand;
    
	public SubmitScreen () {
		//#style submitPopup
		super("Submit Form");
		
		//Command selectCommand = new Command("OK", Command.OK, 1);
		
		//#style submitYesNo
		cg = new ChoiceGroup(Localization.get("question.SendNow"), ChoiceGroup.EXCLUSIVE);
		
		//NOTE! These Indexes are optimized to be added in a certain
		//order. _DO NOT_ change it without updating the static values
		//for their numerical order.
		cg.append(SEND_NOW_DEFAULT_STRING, null);
		cg.append(SEND_LATER_STRING, null);
		cg.append(SEND_NOW_SPEC_STRING, null);//clients wont need to see this
				
		append(cg);
		
		append(new Spacer(80, 0));
	}
	
	public int getCommandChoice() {
		return cg.getSelectedIndex();
	}
	public Object getScreenObject() {
		return this;
	}
	
}
