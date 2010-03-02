package org.javarosa.formmanager.view.summary;

import org.javarosa.core.api.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.utility.SortedIndexSet;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.List;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

public class FormSummaryView extends List {
	private FormEntryModel model;
	private SortedIndexSet indexSet;

	public final Command CMD_EXIT = new Command(Localization.get("menu.Exit"),
			Command.EXIT, 4);
	public final Command CMD_SAVE_EXIT = new Command(Localization
			.get("menu.SaveAndExit"), Command.SCREEN, 4);

	public FormSummaryView(FormEntryModel model) {
		// #style View_All_Form
		super("Form Overview", List.IMPLICIT);
		this.model = model;
		createView();

		addCommand(CMD_EXIT);
		
		// TODO: handle readonly
		if (true) {
			addCommand(CMD_SAVE_EXIT);
		}
	}

	protected void createView() {
		indexSet = new SortedIndexSet();

		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();
		while (!index.isEndOfFormIndex()) {
			if (index.isInForm() && model.isRelevant(index)) {
				if (model.getEvent(index) == FormEntryController.EVENT_QUESTION) {
					FormEntryPrompt prompt = model.getQuestionPrompt(index);
					String styleName = getStyleName(prompt);
					String line = prompt.getLongText() + " => ";

					IAnswerData answerValue = prompt.getAnswerValue();
					if (answerValue != null) {
						line += answerValue.getDisplayText();
					}
					Style style = styleName == null ? null : StyleSheet
							.getStyle(styleName);
					append(line, null, style);
					indexSet.add(index);
				}
			}
			index = form.incrementIndex(index);
		}
	}

	private String getStyleName(FormEntryPrompt prompt) {
		if (prompt.isRequired() && prompt.getAnswerValue() == null) {
			return Constants.STYLE_COMPULSORY;
		}
		return null;
	}

	public FormIndex getFormIndex() {
		int selectedIndex = getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}
		FormIndex formIndex = (FormIndex) indexSet.get(selectedIndex);
		return formIndex;
	}

	public void show() {
		J2MEDisplay.setView(this);
	}
}
