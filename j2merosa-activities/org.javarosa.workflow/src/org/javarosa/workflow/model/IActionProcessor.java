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

import org.javarosa.core.model.IFormDataModel;

/**
 * After a WorkflowAction has been completed, an Action Processor
 * occurs for that workflow action to allow for complicated logic
 * to be executed.
 * 
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public interface IActionProcessor {
	
	public void setOutputData(IFormDataModel model);
	
	public String produceOutput();
	
	public void processFieldOutcomes();
}
