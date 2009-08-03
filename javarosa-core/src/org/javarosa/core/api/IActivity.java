/*
 * Copyright (C) 2009 JavaRosa
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

import org.javarosa.core.Context;


/**
 * An activity encapsulates a discrete execution unit, with the
 * ability to capture and release control flow from a shell.
 * 
 * @author Brian DeRenzi
 * @author Clayton Sims
 * @author Drew Roos
 *
 */
public interface IActivity {

	/**
	 * Starts the module with the current context.
	 * 
	 * @param context The context to run the current module in
	 */
	public void start(Context context);
	
	/**
	 * Updates the context for this module
	 * 
	 * @param globalContext The context object representing the global state 
	 * of the app
	 */
	public void contextChanged(Context globalContext);
	
	/**
	 * Halts any running execution and returns a context for the current state of
	 * the module.
	 * 
	 * @return The current context of the module 
	 */
	public void halt();
	
	/**
	 * Resumes execution of the module after it is halted, generally taking into
	 * account the state of the context 
	 */
	public void resume(Context globalContext);
	
	/**
	 * Cleans up anything that needs to be manually destroyed in a module
	 */
	public void destroy();
	
	/**
	 * @return The context under which the activity is operating
	 */
	public Context getActivityContext();
	
	/**
	 * @param shell the shell that this activity is running under.
	 */
	public void setShell(IShell shell);
	
	/**
	 * Annotates the activity with the provided general command.
	 * 
	 * @param command A wrapper for a platform specific command
	 * action.
	 */
	public void annotateCommand(ICommand command);
}
