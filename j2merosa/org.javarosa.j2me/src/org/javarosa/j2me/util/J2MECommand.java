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
