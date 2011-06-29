package org.javarosa.demo.activity.savedformlist;

import org.javarosa.core.model.instance.FormInstance;

public interface JRDemoSavedFormListTransitions {

	void back();

	void savedFormSelected(int formId,int instanceId);
	
	public void sendDataFormInstance(FormInstance data);
	
}
