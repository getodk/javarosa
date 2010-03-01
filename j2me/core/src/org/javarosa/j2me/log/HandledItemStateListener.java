package org.javarosa.j2me.log;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;

/**
 * This is a wrapper for the ItemStateListener interface that aids in providing top-level exception
 * trapping and logging. See HandledCommandListener for details and usage (following the pattern described
 * there).
 * 
 * @author Drew Roos
 *
 */
public interface HandledItemStateListener extends ItemStateListener {

	void _itemStateChanged (Item i);
	
}
