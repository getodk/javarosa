package org.javarosa.formmanager.view.summary;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Dimension;
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
	private static String STYLE_HEADER = "SingleQuestionScreen_Header";
	
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
		int captioncount = -1;

		indexHash = new HashMap();

		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();
		while (!index.isEndOfFormIndex()) {
			if (index.isInForm() && model.isRelevant(index)) {
				System.out.println("Index: " + index + ", Mult: "
						+ index.getElementMultiplicity() + ", Event: "
						+ model.getEvent(index));
				String text = "";
				boolean isHeader = false;
				if (model.getEvent(index) == FormEntryController.EVENT_QUESTION) {
					FormEntryPrompt prompt = model.getQuestionPrompt(index);
					text = getText(prompt);
				} else if ((model.getEvent(index) == FormEntryController.EVENT_GROUP)
						|| (model.getEvent(index) == FormEntryController.EVENT_REPEAT)) {
					text = getText(model.getCaptionHierarchy(index));
					isHeader = true;
				}
				if (!text.equals("")) {
					Style style = isHeader?StyleSheet.getStyle(STYLE_HEADER):getStyle(index.getDepth()-1);
					style.addAttribute("margin-left", new Dimension((5*(index.getDepth()-1))+"px"));
					append(text, null,style);
					captioncount++;
					indexHash.put(new Integer(captioncount), index);
					System.out.println(text);
				}
			}
			index = form.incrementIndex(index);
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

	private String getText(FormEntryCaption[] hierachy) {
		String headertext = "";
		for (FormEntryCaption caption : hierachy) {
			headertext += caption.getLongText() + ": ";
		}
		if (headertext.endsWith(": "))
			headertext = headertext.substring(0, headertext.length() - 2);
		return headertext;
	}

	private Style getStyle(int level) {
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
