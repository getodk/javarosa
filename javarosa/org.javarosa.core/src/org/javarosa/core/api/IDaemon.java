/*
 * Copyright (C) 2009 JavaRosa-Core Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
