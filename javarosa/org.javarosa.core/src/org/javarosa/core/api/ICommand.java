package org.javarosa.core.api;

/**
 * ICommand is a stand-in wrapper for the same concept as 
 * a "Command" in j2me. Essentially an ICommand represents
 * an optional action that should be present while in static
 * 'homebase' screens for activities. 
 * 
 * This interface should primarily be used to prevent the
 * duplication of high-level functionality across different
 * application workflows. 
 * 
 * If this command object is acted upon inside of an activity,
 * the activity should suspend itself, and return the command
 * as the primary return value, so that it can be acted upon
 * reasonably by the shell.
 * 
 * Commands which should be acted upon inside of an activity are
 * a poor candidate for wrapping in the ICommand structure.
 * 
 * @author Clayton Sims
 * @date Jan 14, 2009 
 *
 */
public interface ICommand {
	/** 
	 * @return A Platform Specific Command object 
	 */
	public Object getCommand();
	
	/**
	 * @return A unique command id that can be used to 
	 * distinguish this command at the shell level.
	 */
	public String getCommandId();
}
