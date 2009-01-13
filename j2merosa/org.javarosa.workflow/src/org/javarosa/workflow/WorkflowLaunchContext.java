/**
 * 
 */
package org.javarosa.workflow;

import org.javarosa.core.Context;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.workflow.model.Workflow;

/**
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class WorkflowLaunchContext extends Context {
	private static String WORKFLOW_KEY = "w";
	private static String DATA_MODEL_KEY = "dm";
	
	public void setWorkflow(Workflow w) {
		this.setElement(WORKFLOW_KEY, w);
	}
	
	public Workflow getWorkflow() {
		return (Workflow)this.getElement(WORKFLOW_KEY);
	}
	
	public void setDataModel(IFormDataModel dm) {
		this.setElement(DATA_MODEL_KEY, dm);
	}
	
	public IFormDataModel getDataModel() {
		return (IFormDataModel)this.getElement(DATA_MODEL_KEY);
	}
	
	
}
