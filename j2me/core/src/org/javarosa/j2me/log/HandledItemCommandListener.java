package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

/**
 * This is a wrapper for the ItemCommandListener interface that aids in providing top-level exception
 * trapping and logging. See HandledCommandListener for details and usage (following the pattern described
 * there).
 * 
 * @author Drew Roos
 *
 */
public interface HandledItemCommandListener extends ItemCommandListener {

	void _commandAction (Command c, Item i);
	
}
