package org.javarosa.formmanager.utility;

import org.javarosa.core.model.FormIndex;

public interface FormEntryModelListener {
	void questionIndexChanged (FormIndex questionIndex);
	void saveStateChanged (int instanceID, boolean dirty);
	void formComplete ();
	void startOfForm ();
}