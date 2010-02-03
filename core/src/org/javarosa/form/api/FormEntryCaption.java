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

package org.javarosa.form.api;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.formmanager.view.IQuestionWidget;

/**
 * This class gives you all the information you need to display a caption when
 * your current FormIndex references a GroupEvent, RepeatPromptEvent, or
 * RepeatEvent.
 * 
 * @author Simon Kelly
 */
public class FormEntryCaption implements FormElementStateListener {

	FormDef form;
	FormIndex index;
	protected IFormElement element;

	protected IQuestionWidget viewWidget;

	/**
	 * This empty constructor exists for convenience of any supertypes of this
	 * prompt
	 */
	public FormEntryCaption() {
	}

	/**
	 * Creates a FormEntryCaption for the element at the given index in the form.
	 * 
	 * @param form
	 * @param index
	 */
	public FormEntryCaption(FormDef form, FormIndex index) {
		this.form = form;
		this.index = index;
		this.element = form.getChild(index);
		this.viewWidget = null;
	}

	public String getLongText() {
		String longText = element.getLongText();
		return substituteStringArgs(longText);
	}

	public String getShortText() {
		String shortText = element.getShortText();
		return substituteStringArgs(shortText);
	}

	public String substituteStringArgs(String templateStr) {
		if (templateStr == null) {
			return null;
		}
		return form.fillTemplateString(templateStr, index.getReference());
	}

	public int getMultiplicity() {
		return index.getElementMultiplicity();
	}

	public IFormElement getFormElement() {
		return element;
	}

	/**
	 * @return true if this represents a <repeat> element
	 */
	public boolean repeats() {
		if (element instanceof GroupDef) {
			return ((GroupDef) element).getRepeat();
		} else {
			return false;
		}
	}

	public FormIndex getIndex() {
		return index;
	}

	// ==== observer pattern ====//

	public void register(IQuestionWidget viewWidget) {
		this.viewWidget = viewWidget;
		element.registerStateObserver(this);
	}

	public void unregister() {
		this.viewWidget = null;
		element.unregisterStateObserver(this);
	}

	public void formElementStateChanged(IFormElement element, int changeFlags) {
		if (this.element != element)
			throw new IllegalStateException(
					"Widget received event from foreign question");
		if (viewWidget != null)
			viewWidget.refreshWidget(changeFlags);
	}

	public void formElementStateChanged(TreeElement instanceNode,
			int changeFlags) {
		throw new RuntimeException("cannot happen");
	}

}
