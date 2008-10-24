package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IDataPointer;
import org.javarosa.core.model.data.PointerAnswerData;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

/**
 * This class represents a small widget to do an image chooser activity.  It
 * is basically a small display that does a pass through to the ImageChooser
 * 
 * @author Cory Zue
 *
 */

public class ImageChooserWidget extends ExpandedWidget {

	private IDataPointer data;
	
	protected IAnswerData getWidgetValue() {
		return new PointerAnswerData(data);
	}

	
	protected void setWidgetValue(Object o) {
		// TODO
		
	}


	protected void updateWidget(QuestionDef question) {
		// TODO Auto-generated method stub
		
	}

	public int widgetType() {
		// TODO Auto-generated method stub
		return Constants.CONTROL_IMAGE_CHOOSE;
	}
	
	protected Item getEntryWidget (QuestionDef question) {
		//#style textBox
		TextField tf = new TextField("", "", 200, TextField.ANY);
		tf.setInputMode(0);
		return tf;
	}



}
