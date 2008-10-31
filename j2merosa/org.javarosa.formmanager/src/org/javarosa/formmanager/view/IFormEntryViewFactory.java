package org.javarosa.formmanager.view;

import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;

public interface IFormEntryViewFactory {
	IFormEntryView getFormEntryView (String viewType, FormEntryModel model, FormEntryController controller);
}
