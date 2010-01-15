package org.javarosa.form.api;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;

public class FormEntryCaption {

	FormDef form;
	FormIndex index;
	private GroupDef groupDef;
	private QuestionDef questionDef;

	public FormEntryCaption() {
	}

	public FormEntryCaption(FormDef form, FormIndex index) {
		this.form = form;
		this.index = index;

		IFormElement element = form.getChild(index);
		if (element instanceof GroupDef) {
			this.groupDef = (GroupDef) element;
		} else if (element instanceof QuestionDef) {
			this.questionDef = (QuestionDef) element;
		} else {
			throw new IllegalArgumentException(
					"Unexpected type of IFormElement");
		}
	}

	protected QuestionDef getQuestionDef() {
		return questionDef;
	}

	public String getLongText() {
		String longText = groupDef == null ? questionDef.getLongText()
				: groupDef.getLongText();
		return substituteStringArgs(longText);
	}

	public String getShortText() {
		String shortText = groupDef == null ? questionDef.getShortText()
				: groupDef.getShortText();
		return substituteStringArgs(shortText);
	}

	public String substituteStringArgs(String templateStr) {
		return form.fillTemplateString(templateStr, index.getReference());
	}
}