package org.javarosa.formmanager.utility;

public interface FormEntryModelListener {
	void questionIndexChanged (int questionIndex);
	void saveStateChanged (int instanceID, boolean dirty);
}