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

package org.javarosa.formmanager.view.widgets;

import javax.microedition.lcdui.Command;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultiPointerAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

/**
 * This class represents a small widget to do an image chooser activity.  It
 * is basically a small display that does a pass through to the ImageChooser
 * 
 * @author Cory Zue
 *
 */

public class ImageChooserWidget extends ExpandedWidget {

	private MultiPointerAnswerData data;
	
	private StringItem label;
	
	protected IAnswerData getWidgetValue() {
		return data;
	}

	
	protected void setWidgetValue(Object o) {
		IDataPointer[] castedO = (IDataPointer[]) o;
		if (castedO != null && castedO.length > 0) {
			data = new MultiPointerAnswerData((IDataPointer[]) o);
		} else {
			data = null;
		}
		updateLabel();
	}


	protected void updateWidget(FormEntryPrompt prompt) {
		// do nothing? 
		
	}

	public int widgetType() {
		return Constants.CONTROL_IMAGE_CHOOSE;
	}
	
	protected Item getEntryWidget (FormEntryPrompt prompt) {
		// //#style textBox
		
		updateLabel();
		return label;
	}


	private void updateLabel() {
		
		if (label == null) {
			label = new StringItem("", "");
			// this command comes from the other constants file
			Command cameraCommand = new Command(org.javarosa.core.api.Constants.ACTIVITY_TYPE_GET_IMAGES, "Get Images", Command.SCREEN, 0);
			label.addCommand(cameraCommand);
		}
		if (data == null) {
			label.setLabel("Use the menu to get images");
			label.setText("No images selected");
		} else {
			label.setLabel("Use the menu item to change selected images");
			label.setText(data.getDisplayText());
		}
	}

	protected IAnswerData getAnswerTemplate() {
		return new MultiPointerAnswerData();
	}

}
