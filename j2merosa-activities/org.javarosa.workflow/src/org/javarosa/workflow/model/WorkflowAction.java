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

/**
 * 
 */
package org.javarosa.workflow.model;


/**
 * A workflow action encapsulates a possible interaction with a user,
 * along with a process that can be used to create a meaningful information
 * output to the user, along with a description of the next state that
 * should be traversed to.
 * 
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class WorkflowAction {
	
	/**
	 * A for-user description of the action.
	 */
	private String description;
	
	/**
	 * 
	 */
	private String action;
	
	private IActionProcessor processor;
	
	public WorkflowAction(String action, String description, IActionProcessor processor) {
		this.action = action;
		this.description = description;
		this.processor = processor;
	}
	
	/**
	 * @return A human-interpretable description of what this action
	 * will consist of.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return The coded action that should be executed by the workflow
	 * in order to traverse this edge. The workflow should be able to
	 * process and meaningfully carry out the acton codified by the string
	 * returned from this method. 
	 */
	public String getAction() {
		return action;
	}
	
	public IActionProcessor getProcessor() {
		return processor;
	}
}
