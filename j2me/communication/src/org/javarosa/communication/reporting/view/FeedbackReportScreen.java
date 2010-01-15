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

/**
 * 
 */
package org.javarosa.communication.reporting.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * @author Clayton Sims
 * @date Feb 27, 2009 
 *
 */
public class FeedbackReportScreen extends TextBox {
	public static final Command SEND_REPORT = new Command("Send Report", 1, Command.OK);
	public static final Command CANCEL = new Command("Cancel", 2, Command.CANCEL);
	

	public FeedbackReportScreen(String title) {
		super("Please enter your feedback report.", "", 500, TextField.ANY);
		this.addCommand(SEND_REPORT);
		this.addCommand(CANCEL);
	}

}
