/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.demo.util;

import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.IFormEntryViewFactory;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.clforms.FormViewManager;

public class FormEntryViewFactory implements IFormEntryViewFactory {
	public IFormEntryView getFormEntryView (String viewType, FormEntryModel model, FormEntryController controller) {
		if (FormManagerProperties.VIEW_CHATTERBOX.equals(viewType)) {
			return new Chatterbox("Chatterbox", model, controller);
		} else if (FormManagerProperties.VIEW_CLFORMS.equals(viewType)) {
			return new FormViewManager("CLForms", model, controller);
		}
		else {
			return null;
		}
	}
}
