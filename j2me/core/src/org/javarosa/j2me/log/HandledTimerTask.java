package org.javarosa.j2me.log;

import java.util.TimerTask;

import org.javarosa.core.services.Logger;

/**
 * This is a wrapper class around TimerTask that provides top-level exception trapping and logging
 * for any code that runs in this task's thread. See HandledThread for details of this class's purpose.
 * 
 * To use, change:
 * class myTask extends TimerTask {
 *   public void run () {
 *     ...
 *     
 * to:
 * class myTask extends HandledTimerTask {
 *   public void _run () {
 *     ...
 * 
 * @author Drew Roos
 *
 */
public abstract class HandledTimerTask extends TimerTask {

	public final void run () {
		try {
			_run();
		} catch (Exception e) {
			Logger.die("timer", e);
		}
	}
	
	public abstract void _run ();
	
}
