package org.javarosa.j2me.log;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.services.IncidentLogger;

public class CrashHandler {

	public static void commandAction(HandledCommandListener handler, Command c, Displayable d) {
		try {
			handler._commandAction(c, d);
		} catch (Exception e) {
			IncidentLogger.die("gui", e);
		}
	}
}
