package org.javarosa.formmanager.view.summary;

import org.javarosa.core.model.FormIndex;

public interface FormSummaryTransitions {

	void exit();

	void saveAndExit(boolean formComplete);

	void viewForm(FormIndex formIndex);

}
