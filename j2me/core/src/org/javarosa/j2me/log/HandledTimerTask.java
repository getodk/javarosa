package org.javarosa.j2me.log;

import java.util.TimerTask;

import org.javarosa.core.services.IncidentLogger;

public abstract class HandledTimerTask extends TimerTask {

	public final void run () {
		try {
			_run();
		} catch (Exception e) {
			IncidentLogger.die("timer", e);
		}
	}
	
	public abstract void _run ();
	
}
