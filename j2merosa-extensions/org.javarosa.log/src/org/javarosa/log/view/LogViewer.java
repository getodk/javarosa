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
package org.javarosa.log.view;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogViewer extends Form implements IView {

	StringItem logs;
	
	public LogViewer() {
		super("Incident Log");
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}
	
	public void loadLogs(String data) {
		logs = new StringItem("", data);
		this.append(logs);
	}
}
