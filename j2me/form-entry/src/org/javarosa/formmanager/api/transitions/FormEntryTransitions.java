package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;

public interface FormEntryTransitions {
	final int MEDIA_IMAGE = 1;
	final int MEDIA_AUDIO = 2;
	final int MEDIA_VIDEO = 3;

	public void abort();
	
	public void formEntrySaved(FormDef form, DataModelTree instanceData, boolean formWasCompleted);
	
	public void suspendForMediaCapture (int captureType);
	
}