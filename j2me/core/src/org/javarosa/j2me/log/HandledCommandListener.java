package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

/**
 * This is a wrapper for the CommandListener interface that aids in providing top-level exception
 * trapping and logging. Exceptions in J2ME can only be caught by a try/catch block running in the
 * same stack that the exception originated. Since GUI events are dispatched in the GUI thread, to
 * log all exceptions in the GUI handling code and all code lauched by the GUI handling code (which
 * is the bulk of the javarosa codebase), we must set exception traps at any point that the GUI
 * thread(s) invoke javarosa code.
 * 
 * To use:
 * 
 * 1) replace classes that implement CommandListener with HandledCommandListener
 * 
 * 2) change the old definition of commandAction to _commandAction
 * 
 * 3) redefine commandAction() to:
 * public void commandAction(Command c, Displayable d) {
 *   CrashHandler.commandAction(this, c, d);
 * }
 * (CrashHandler sets up the exception trap then delegates back to _commandAction())
 * 
 * @author Drew Roos
 *
 */
public interface HandledCommandListener extends CommandListener {

	void _commandAction (Command c, Displayable d);
	
}
