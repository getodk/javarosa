package org.javarosa.workflow;

import org.javarosa.core.Context;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.workflow.model.IActionProcessor;

public class PostActionProcessContext extends Context {
	private static String ACTION_PROCESS_KEY = "ap";
	private static String DATA_MODEL_KEY = "dm";
	
	public void setActionProcessor(IActionProcessor ap) {
		this.setElement(ACTION_PROCESS_KEY, ap);
	}
	
	public IActionProcessor getActionProcessor() {
		return (IActionProcessor)this.getElement(ACTION_PROCESS_KEY);
	}
	
	public void setDataModel(IFormDataModel dm) {
		this.setElement(DATA_MODEL_KEY, dm);
	}
	
	public IFormDataModel getDataModel() {
		return (IFormDataModel)this.getElement(DATA_MODEL_KEY);
	}
}
