package org.javarosa.formmanager.view.summary;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.List;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.util.HashMap;

public class FormSummaryView extends List {
	private FormEntryModel model;
	private HashMap indexHash;

	private static String STYLE_NOGROUP = "SingleQuestionScreen_No_Group";
	private static String STYLE_GROUP = "SingleQuestionScreen_Group";
	private static String STYLE_GROUP_ALT = "SingleQuestionScreen_Group_Alt";

	public final Command CMD_EXIT = new Command(Localization.get("menu.Exit"),
			Command.EXIT, 4);
	public final Command CMD_SAVE_EXIT = new Command(Localization
			.get("menu.SaveAndExit"), Command.SCREEN, 4);

	public FormSummaryView(FormEntryModel model) {
		// #style SingleQuestionScreen_Form
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
		int level = 0;
		int captioncount = - 1;

		indexHash = new HashMap();

		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();
		while (!index.isEndOfFormIndex()) {
			if (index.isInForm() && model.isRelevant(index)) {
				if (model.getEvent(index) == FormEntryController.EVENT_QUESTION) {
					FormEntryCaption[] hierachy = model
							.getCaptionHierarchy(index);
					for (FormEntryCaption caption : hierachy) {
						captioncount++;
						if (caption.getFormElement() instanceof GroupDef) {
							if (!caption.getFormElement().getChildren()
									.isEmpty()) { // start of group
								System.out
										.print("start of group. level changed from "
												+ level + " to ");
								level++;
								System.out.println(level);
							} else { // end of group
								System.out
										.print("end of group. level changed from "
												+ level + " to ");
								level--;
								System.out.println(level);
							}
						}
						String text = getText(caption);
						append(text, null, getStyleForLevel(level));
						indexHash.put(new Integer(captioncount), index);
					}
				}
			}
			index = form.incrementIndex(index);
		}
	}

	private String getText(FormEntryCaption caption) {
		String line = "";
		if (caption instanceof FormEntryPrompt) {
			FormEntryPrompt prompt = (FormEntryPrompt) caption;
			line += prompt.getLongText() + " => ";
			if (prompt.isRequired() && prompt.getAnswerValue() == null) {
				line = "*" + line;
			}
			IAnswerData answerValue = prompt.getAnswerValue();
			if (answerValue != null) {
				line += answerValue.getDisplayText();
			}
		} else if (!caption.getFormElement().getChildren().isEmpty()) {
			line += caption.getLongText();
		}

		return line;
	}

	private Style getStyleForLevel(int level) {
		Style style = null;
		if (level == 0) {
			style = StyleSheet.getStyle(STYLE_NOGROUP);
		} else if ((level % 2) == 0) {
			style = StyleSheet.getStyle(STYLE_GROUP);
		} else {
			style = StyleSheet.getStyle(STYLE_GROUP_ALT);
		}
		return style;
	}

	public FormIndex getFormIndex() {
		int selectedIndex = getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}
		FormIndex formIndex = (FormIndex) indexHash.get(new Integer(
				selectedIndex));
		return formIndex;
	}

	public void show() {
		J2MEDisplay.setView(this);
	}
}
