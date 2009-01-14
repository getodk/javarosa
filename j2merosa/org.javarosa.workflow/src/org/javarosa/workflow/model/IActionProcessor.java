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
