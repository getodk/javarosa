package org.javarosa.core.util;

import java.util.Hashtable;
import java.util.Stack;

import org.javarosa.core.api.IModule;


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
	
	public void push(IModule module) {
		internalStack.push(module);
	}
	
	public int size() {
		return internalStack.size();
	}
	
	public IModule pop() {
		IModule module = (IModule)internalStack.pop();
		return module;
	}
}
