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

import java.util.Hashtable;

/**
 * Shells are responsible for controlling the workflow of a 
 * JavaRosa application. It is responsibly for spawning activities,
 * managing their returns, and mitigating access to the Application's 
 * display. 
 * 
 * @author Clayton Sims
 *
 */
public interface IShell {
	/**
	 * Called when this IShell should start to run
	 */
	void run();
	
	/**
	 * Called when a module has completed running and should return control here.
	 */
	void returnFromActivity(IActivity activity, String returnCode, Hashtable returnArgs);

	/**
	 * Called when this IShell is being exited.  This could be another application loading of this application quitting.
	 */
	void exitShell();

	/**
	 * Sets the current display, taking into account what module is currently executing 
	 * 
	 * @param callingModule The module attempting to set the displayable
	 * @param display The display to be set
	 */
	boolean setDisplay(IActivity callingActivity, IView display);
	
}
