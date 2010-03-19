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

package org.javarosa.formmanager.view;

import org.javarosa.services.transport.TransportListener;

public interface ISubmitStatusObserver extends TransportListener {
	/**
	 * Destroys the current status screen and cleans up any running
	 * processes.
	 */
	public void destroy();
	
	/**
	 * 
	 */
	
	/**
	 * Receive a message from the sender which breaks the contract between
	 * the sender and the screen .
	 * 
	 * @param message A coded message for an issue which arose
	 * @param details The details (if any) of the failure
	 */
	public void receiveError(String details); 
}
