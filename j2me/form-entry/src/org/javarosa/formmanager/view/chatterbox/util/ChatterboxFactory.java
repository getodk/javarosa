/**
 * 
 */
package org.javarosa.formmanager.view.chatterbox.util;

import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.IFormEntryViewFactory;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

/**
 * @author ctsims
 *
 */
public class ChatterboxFactory implements IFormEntryViewFactory {

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.IFormEntryViewFactory#getFormEntryView(java.lang.String, org.javarosa.formmanager.model.FormEntryModel, org.javarosa.formmanager.controller.FormEntryController)
	 */
	public IFormEntryView getFormEntryView(String viewType, FormEntryModel model, FormEntryController controller) {
		return new Chatterbox(viewType, model, controller);
	}

}
