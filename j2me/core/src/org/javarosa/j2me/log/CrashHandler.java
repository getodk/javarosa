package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

import org.javarosa.core.services.IncidentLogger;

public class CrashHandler {

	public static void commandAction(HandledCommandListener handler, Command c, Displayable d) {
		try {
			handler._commandAction(c, d);
		} catch (Exception e) {
			IncidentLogger.die("gui-cl", e);
		}
	}
	
	public static void commandAction(HandledPCommandListener handler,
			de.enough.polish.ui.Command c, de.enough.polish.ui.Displayable d) {
		try {
			handler._commandAction(c, d);
		} catch (Exception e) {
			IncidentLogger.die("gui-clp", e);
		}
	}
	
	public static void commandAction(HandledItemCommandListener handler, Command c, Item i) {
		try {
			handler._commandAction(c, i);
		} catch (Exception e) {
			IncidentLogger.die("gui-icl", e);
		}
	}
	
	public static void commandAction(HandledPItemCommandListener handler,
			de.enough.polish.ui.Command c, de.enough.polish.ui.Item i) {
		try {
			handler._commandAction(c, i);
		} catch (Exception e) {
			IncidentLogger.die("gui-iclp", e);
		}
	}
	
	public static void itemStateChanged(HandledItemStateListener handler, Item i) {
		try {
			handler._itemStateChanged(i);
		} catch (Exception e) {
			IncidentLogger.die("gui-isl", e);
		}
	}
	
	public static void itemStateChanged(HandledPItemStateListener handler, de.enough.polish.ui.Item i) {
		try {
			handler._itemStateChanged(i);
		} catch (Exception e) {
			IncidentLogger.die("gui-islp", e);
		}
	}
}
