package org.javarosa.j2me.log.viewer;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.api.State;
import org.javarosa.core.log.IFullLogSerializer;
import org.javarosa.core.log.LogEntry;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.Logger;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;

public abstract class LogViewerState implements State, TrivialTransitions, HandledCommandListener {

	static final int DEFAULT_MAX_ENTRIES = 4;
	
	int max_entries;
	Command exit;
	
	public LogViewerState () {
		this(DEFAULT_MAX_ENTRIES);
	}
	
	public LogViewerState (int max_entries) {
		this.max_entries = max_entries;
	}
	
	public void start () {
		Vector<String> logData = Logger._().serializeLogs(new IFullLogSerializer<Vector<String>>() {
			public Vector<String> serializeLogs(LogEntry[] logs) {
				Vector<String> msgs = new Vector<String>();
				if (logs == null) {
					msgs.addElement("error reading logs!");
				} else {
					int count = Math.min(logs.length, max_entries > 0 ? max_entries : logs.length);
					String prevDateStr = null;
					for (int i = 0; i < count; i++) {
						LogEntry entry = logs[i];
						
						String fullDateStr = DateUtils.formatDateTime(entry.getTime(), DateUtils.FORMAT_ISO8601).substring(0, 19);
						String dateStr = fullDateStr.substring(11);
						if (prevDateStr == null || !fullDateStr.substring(0, 10).equals(prevDateStr.substring(0, 10))) {
							msgs.addElement("= " + fullDateStr.substring(0, 10) + " =");
						}
						prevDateStr = fullDateStr;
						
						String line = dateStr + ":" + entry.getType() + "> " + entry.getMessage(); 
						msgs.addElement(line);						
					}
					if (count < logs.length) {
						msgs.addElement("(" + (logs.length - count) + " more entries...)");
					}
				}
				return msgs;
			}
		});

		Form view = new Form("logs");
		for (int i = 0; i < logData.size(); i++) {
			view.append(new StringItem("", logData.elementAt(i)));
		}
		exit = new Command("OK", Command.BACK, 0);
		view.setCommandListener(this);
		view.addCommand(exit);
		J2MEDisplay.setView(view);
	}
		
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}
	
	public void _commandAction(Command c, Displayable d) {
		if (c == exit)
			done();
	}
	
	//nokia s40 bug
	public abstract void done ();
}
