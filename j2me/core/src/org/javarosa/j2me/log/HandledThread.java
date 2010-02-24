package org.javarosa.j2me.log;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.services.IncidentLogger;

public class HandledThread extends Thread {
	private boolean withRunnable = false;
	
	public HandledThread () {
		super();
	}
	
	public HandledThread (String s) {
		super(s);
	}
	
	public HandledThread (Runnable r) {
		super(wrappedRunnable(r));
		withRunnable = true;
	}
	
	public HandledThread (Runnable r, String s) {
		super(wrappedRunnable(r), s);
		withRunnable = true;
	}
	
	private static Runnable wrappedRunnable (final Runnable r) {
		return new Runnable () {
			public void run() {
				try {
					r.run();
				} catch (Exception e) {
					IncidentLogger.die("thread-r", e);
				}
			}
		};
	}
	
	public final void run () {
		if (withRunnable) {
			super.run();
		} else {		
			try {
				_run();
			} catch (Exception e) {
				IncidentLogger.die("thread", e);
			}
		}
	}
	
	public void _run () {
		throw new FatalException("_run method must be overwritten by child");
	}
	
}
