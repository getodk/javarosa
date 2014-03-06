package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

import org.javarosa.core.services.Logger;

/**
 * This class provides exception-handling wrappers for the GUI event interfaces CommandListener,
 * ItemStateListener, etc.
 * 
 * The entry point of the interface (i.e, CommandListener.commandAction()) calls out to this class,
 * which sets up an exception trap, then delegates back to the calling class via a helper method.
 * 
 * @author Drew Roos
 *
 */
public class CrashHandler {
	
	/**
	 * other places exceptions need to be explicitly trapped
	 * 
	 * 1) anywhere that app code is called from a background thread or timeout (SplashScreen,
	 *    network callbacks (if any?))
	 * 2) anywhere a GUI event handler can trigger app code; most of these are handled by the
	 *    wrappers here, but there are still places such as key*(), pointer*(), (handle)Key*(),
	 *    and (handle)Pointer*()
	 * 3) anywhere the app code launches a Runnable (thread or timertask)
	 * 
	 */
	
	public static void commandAction(HandledCommandListener handler, Command c, Displayable d) {
		try {
			handler._commandAction(c, d);
		} catch (Exception e) {
			Logger.die("gui-cl", e);
		}
	}
	
	public static void commandAction(HandledPCommandListener handler,
			de.enough.polish.ui.Command c, de.enough.polish.ui.Displayable d) {
		try {
			handler._commandAction(c, d);
		} catch (Exception e) {
			Logger.die("gui-clp", e);
		}
	}
	
	public static void commandAction(HandledItemCommandListener handler, Command c, Item i) {
		try {
			handler._commandAction(c, i);
		} catch (Exception e) {
			Logger.die("gui-icl", e);
		}
	}
	
	public static void commandAction(HandledPItemCommandListener handler,
			de.enough.polish.ui.Command c, de.enough.polish.ui.Item i) {
		try {
			handler._commandAction(c, i);
		} catch (Exception e) {
			Logger.die("gui-iclp", e);
		}
	}
	
	public static void itemStateChanged(HandledItemStateListener handler, Item i) {
		try {
			handler._itemStateChanged(i);
		} catch (Exception e) {
			Logger.die("gui-isl", e);
		}
	}
	
	public static void itemStateChanged(HandledPItemStateListener handler, de.enough.polish.ui.Item i) {
		try {
			handler._itemStateChanged(i);
		} catch (Exception e) {
			Logger.die("gui-islp", e);
		}
	}

}
