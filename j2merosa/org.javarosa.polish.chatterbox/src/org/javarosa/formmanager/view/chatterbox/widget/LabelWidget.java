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

package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.FormElementBinding;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public class LabelWidget implements IWidgetStyle {
	private StringItem label;
	
	private int multiplicity = -1;
	
	public LabelWidget() {
		super();
	}
	
	public LabelWidget(int mult) {
		this();
		multiplicity = mult;
	}
	
	public void initWidget(IFormElement element, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		label = new StringItem(null, null);
		
		c.add(label);
	}

	public void refreshWidget(FormElementBinding bind, int changeFlags) {
		
		// Clayton Sims - Feb 6, 2009 : Labels are now applicable for questions, so that we can pin 
		// question texts for long questions.
		if(bind.element instanceof GroupDef) {
			String caption = bind.form.fillTemplateString(((GroupDef)bind.element).getLongText(), bind.instanceRef);
			if (multiplicity != -1) {
				caption += ": " + multiplicity;
			}
			label.setText(caption);
		} else if(bind.element instanceof QuestionDef) {
			String caption = bind.form.fillTemplateString(((QuestionDef)bind.element).getLongText(), bind.instanceRef);
			label.setText(caption);
		}
		else{
			throw new IllegalStateException("Invalid type for a label's element. Type is: " + bind.element.getClass().getName() );
		}
		

	}

	public void reset() {
		label = null;
	}

	public int widgetType() {
		return Constants.CONTROL_LABEL;
	}
	
	public String toString() {
		return label.getText();
	}
	
	public Object clone() {
		LabelWidget label = new LabelWidget(multiplicity);
		return label;
	}

	public int getPinnableHeight() {
		return label.getContentHeight();
	}
	
	public int getMultiplicity() {
		return multiplicity;
	}
}
