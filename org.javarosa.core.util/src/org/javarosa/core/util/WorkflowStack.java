package org.javarosa.core.util;

import java.util.Hashtable;
import java.util.Stack;

import org.javarosa.core.api.IActivity;


/**
 * The workflow stack object maintains a stack of shell or module
 * objects.
 * 
 * @author Clayton Sims
 *
 */
public class WorkflowStack {

	Stack internalStack;
		
	public WorkflowStack() {
		internalStack = new Stack();
	}
	
	public void push(IActivity activity) {
		internalStack.push(activity);
	}
	
	public int size() {
		return internalStack.size();
	}
	
	public IActivity pop() {
		IActivity activity = (IActivity)internalStack.pop();
		return activity;
	}
}
