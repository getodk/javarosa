package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.api.Transitions;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;

public interface FormEntryTransitions extends Transitions {

	public void abort();
	public void formEntrySaved(FormDef form, DataModelTree instanceData, boolean formWasCompleted);
	
	
}