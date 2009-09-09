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
package org.javarosa.j2me.util;

import javax.microedition.lcdui.Command;

import org.javarosa.core.api.ICommand;

/**
 * The J2ME implementation of an ICommand. 
 * 
 * @author Clayton Sims
 * @date Jan 26, 2009 
 *
 */
public class J2MECommand implements ICommand {
	
	Command command;
	String id;
	
	public J2MECommand(Command command, String id) {
		this.command = command;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.ICommand#getCommand()
	 */
	public Object getCommand() {
		return command;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.ICommand#getCommandId()
	 */
	public String getCommandId() {
		return id;
	}

}
