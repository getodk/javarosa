package org.javarosa.formmanager.view.summary;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.singlequestionscreen.Constants;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Color;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.List;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.borders.BottomBorder;

public class FormSummaryView extends List {
	private FormEntryModel model;

	public final Command CMD_EXIT = new Command(Localization.get("menu.Exit"),
			Command.EXIT, 4);
	public final Command CMD_SAVE_EXIT = new Command(Localization
			.get("menu.SaveAndExit"), Command.SCREEN, 4);

	private Entry[] entries;

	static class Entry extends ChoiceItem {
		FormIndex index;

		Entry(String caption, Image img, Style style, FormIndex index) {
			super(caption, img, List.IMPLICIT);
			this.setStyle(style);
			this.index = index;
		}
	}

	public FormSummaryView(FormEntryModel model) {
		//#style View_All_Form
		super("Form Overview", List.IMPLICIT);
		this.model = model;
		createView();

		addCommand(CMD_EXIT);

		// TODO: handle readonly
		if (true) {
			addCommand(CMD_SAVE_EXIT);
		}
	}

	private void populateEntries() {
		Vector<Entry> entriesVec = new Vector<Entry>();
		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();

		int prevDepth = 0;

		while (!index.isEndOfFormIndex()) {
			if (index.isInForm() && model.isIndexRelevant(index)) {
				String text = "";
				boolean isHeader = false;
				Image img = null;
				if (model.getEvent(index) == FormEntryController.EVENT_QUESTION) {
					FormEntryPrompt prompt = model.getQuestionPrompt(index);
					text = getText(prompt);
				} else if (model.getEvent(index) == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
					FormEntryCaption[] hierachy = model
							.getCaptionHierarchy(index);
					text = "Add "
							+ (index.getElementMultiplicity() == 0 ? "a "
									: "another ")
							+ hierachy[hierachy.length - 1].getLongText() + "?";
					try {
						img = Image.createImage(Localization.get("plussign"));
					} catch (IOException ioe) {
						img = null;
						Logger.exception(ioe);
					}
				} else if ((model.getEvent(index) == FormEntryController.EVENT_GROUP)
						|| (model.getEvent(index) == FormEntryController.EVENT_REPEAT)) {
					text = getHeaderText(model.getCaptionHierarchy(index));
					isHeader = true;
				}
				if (!text.equals("")) {
					Style style = isHeader ? StyleSheet
							.getStyle(Constants.STYLE_HEADER) : StyleSheet
							.getStyle(Constants.STYLE_PROMPT);
					String spacer="";
					int i = isHeader?-1:0;
					while (i < index.getDepth() - 2) {
						spacer = "――" + spacer;
						i++;
					}
					text=img==null?spacer+text:text;
					entriesVec.addElement(new Entry(text, img, style, index));
				}
			}
			prevDepth = index.getDepth();
			index = form.incrementIndex(index);
		}
		entries = new Entry[entriesVec.size()];
		entriesVec.copyInto(entries);
	}

	protected void createView() {
		populateEntries();
		for (Entry entry : entries) {
			append(entry);
		}
	}

	private String getText(FormEntryPrompt prompt) {
		String line = "";
		line += prompt.getLongText() + " => ";
		if (prompt.isRequired() && prompt.getAnswerValue() == null) {
			line = "*" + line;
		}
		IAnswerData answerValue = prompt.getAnswerValue();
		if (answerValue != null) {
			line += answerValue.getDisplayText();
		}

		return line;
	}

	private String getHeaderText(FormEntryCaption[] hierachy) {
		String headertext = "";
		for (FormEntryCaption caption : hierachy) {
			headertext += caption.getLongText();

			if (caption.getIndex().getInstanceIndex() > -1)
				headertext += " " + (caption.getMultiplicity() + 1);

			headertext += ": ";
		}
		if (headertext.endsWith(": "))
			headertext = headertext.substring(0, headertext.length() - 2);

		return headertext;
	}

	public FormIndex getFormIndex() {
		int selectedIndex = getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}
		FormIndex formIndex = ((Entry) getItem(selectedIndex)).index;
		return formIndex;
	}

	public void show() {
		J2MEDisplay.setView(this);
	}
}
