package org.javarosa.j2me.log;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.services.Logger;

/**
 * This is a wrapper class around Thread that provides top-level exception trapping and logging
 * for any code that runs in this thread. It is needed because exceptions thrown in the Thread's
 * stack can only be caught by a try/catch block within the stack.
 * 
 * It is a (near) drop-in replacement for Thread.
 * 
 * If you invoke your thread like:
 *   new Thread(myRunnable).start()
 * just change to:
 *   new HandledThread(myRunnable).start()
 *   
 * If you invoke your thread by subclassing Thread itself, ie:
 * 
 * class myThread extends Thread {
 *   public void run () {
 *     ...
 *   }
 * }
 * new myThread().start()
 * 
 * change to:
 * 
 * class myThread extends HandledThread {
 *   public void _run () { //<--- note underscore
 *     ...
 * 
 * @author Drew Roos
 *
 */
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
					Logger.die("thread-r", e);
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
				Logger.die("thread", e);
			}
		}
	}
	
	public void _run () {
		throw new FatalException("_run method must be overwritten by child");
	}
	
}
