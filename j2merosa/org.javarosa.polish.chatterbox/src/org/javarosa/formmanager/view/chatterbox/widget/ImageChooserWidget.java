package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Command;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultiPointerAnswerData;

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


	protected void updateWidget(QuestionDef question) {
		// do nothing? 
		
	}

	public int widgetType() {
		return Constants.CONTROL_IMAGE_CHOOSE;
	}
	
	protected Item getEntryWidget (QuestionDef question) {
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



}
