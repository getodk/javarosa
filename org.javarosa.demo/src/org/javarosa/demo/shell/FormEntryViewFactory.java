package org.javarosa.demo.shell;

import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.IFormEntryViewFactory;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.clforms.FormViewScreen;

public class FormEntryViewFactory implements IFormEntryViewFactory {
	public IFormEntryView getFormEntryView (String viewType, FormEntryModel model, FormEntryController controller) {
		if (FormManagerProperties.VIEW_CHATTERBOX.equals(viewType)) {
			return new Chatterbox("Chatterbox STRING", model, controller);
		} else if (FormManagerProperties.VIEW_CLFORMS.equals(viewType)) {
			return new FormViewScreen("CL_FORMS STRING", model, controller);
		}
		else {
			return null;
		}
	}
}
