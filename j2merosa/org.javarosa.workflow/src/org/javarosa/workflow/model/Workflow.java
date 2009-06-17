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

package org.javarosa.workflow.model;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;

/**
 * Abstractly: A workflow a state machine which maintains
 * a current state of execution, and provides an interface
 * for listing and traversing possible edges or 'actions',
 * providing a meaningful output after each one is traversed,
 * and providing the interface by which codified actions
 * are used to provide a UI to the user.
 * 
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class Workflow {
	/**Vector<WorkflowState> **/
	Vector states;
	
	/** The model that should be used for evaluating variables
	 * in the state machine.
	 */
	IFormDataModel model;
	
	/** Vector<WorkflowActionListener> **/
	Vector listeners;
	
	public Workflow(Vector states) {
		this.states = states;
	}
	
	public void setDataModel(IFormDataModel model) {
		this.model = model;
	}
	
	protected void processAction(WorkflowAction action) {
		Enumeration en = listeners.elements();
		while(en.hasMoreElements()) {
			((IWorkflowActionListener)en.nextElement()).actionFired(action);
		}
	}
	
	/**
	 * @return The current executing state of this workflow. null
	 * if no states are recognized as being valid.
	 */
	public WorkflowState getCurrentState() { 
		Enumeration en = states.elements();
		
		while(en.hasMoreElements()) {
			WorkflowState state = (WorkflowState)en.nextElement();
			if(state.isCurrentState(model)) {
				return state;
			}
		}
		
		//TODO: Do we want a Factory Static "Start" state like we
		// have with FormIndices?
		return null;
	}
	
	/**
	 * Ends execution of the current workflow.
	 */
	public void terminate() {
		
	}
	
	public void addListener(IWorkflowActionListener listener) {
		listeners.addElement(listener);
	}
}
