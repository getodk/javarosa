package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.api.Transitions;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;

public interface FormEntryTransitions extends Transitions {

	public void abort();
	public void formEntrySaved(FormDef form, DataModelTree instanceData, boolean formWasCompleted);
	
	
}
//if ("exit".equals(status)) {
//	Hashtable returnArgs = new Hashtable();
//
//	returnArgs.put("INSTANCE_ID", new Integer(model.getInstanceID()));
//	returnArgs.put("DATA_MODEL", model.getForm().getDataModel());
//	returnArgs.put("FORM_COMPLETE", new Boolean(model.isFormComplete()));
//	returnArgs.put("QUIT_WITHOUT_SAVING", new Boolean(!model.isSaved()));
//	returnArgs.put(FormEntryContext.FORM_ID, new Integer(model.getForm().getRecordId()));
//
//	if(processor != null && model.isFormComplete() && model.isSaved()) {
//		processor.initializeContext(this.context);
//		processor.processModel(model.getForm().getDataModel());
//		processor.loadProcessedContext(this.context);
//	}
//	
//	parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
