package org.javarosa.demo.shell;

import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.IFormEntryViewFactory;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

public class FormEntryViewFactory implements IFormEntryViewFactory {
	public IFormEntryView getFormEntryView (String viewType, FormEntryModel model, FormEntryController controller) {
		if ("chatterbox".equals(viewType)) {
			return new Chatterbox("Chatterbox", model, controller);
		} else {
			return null;
		}
	}
}
