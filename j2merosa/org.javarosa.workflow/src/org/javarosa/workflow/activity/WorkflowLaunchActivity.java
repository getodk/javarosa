/**
 * 
 */
package org.javarosa.workflow.activity;

import org.javarosa.core.Context;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.workflow.WorkflowLaunchContext;
import org.javarosa.workflow.model.Workflow;

/**
 * WorkflowLaunchActivity is responsible for identifying the current state of a workflow
 * and presenting the appropriate User Interface for that state.
 * 
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class WorkflowLaunchActivity implements IActivity {
	
	/**
	 * The workflow that will be used in this activity.
	 */
	Workflow workflow;
	
	WorkflowLaunchContext context;
	
	IShell shell;

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		if(context instanceof WorkflowLaunchContext) {
			this.context = (WorkflowLaunchContext)context;
			this.workflow = this.context.getWorkflow();
			this.workflow.setDataModel(this.context.getDataModel());
		}
		
	}

}
