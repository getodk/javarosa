package org.javarosa.core.api;

/**
 * A Daemon is a wrapper for a background service that can run
 * concurrently with the main thread and other Daemons. 
 * 
 * Daemons should generally treat start() and stop() as thread
 * invocations and thread destructions respectively. The background
 * services shouldn't be polling threads unless the thread is running
 * in order to preserve device resources.
 * 
 * @author Clayton Sims
 *
 */
public interface IDaemon {

	/**
	 * @return A string uniquely identifying this daemon
	 */
	public String getName();
	
	/**
	 * Starts this daemon's background process.
	 * 
	 * Shouldn't be called if the process is already running.
	 */
	public void start();
	
	/**
	 * Stops this daemon's background process
	 */
	public void stop();

	/**
	 * Attempts to stop the daemon, if it is running, and then
	 * starts it cleanly.
	 */
	public void restart();
	
	/**
	 * @return True if this Daemon's background process is currently
	 * running. False otherwise.
	 */
	public boolean isRunning();
}
