package org.javarosa.formmanager.view.summary;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledPCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.List;

public class FormSummaryController implements HandledPCommandListener {

	private FormSummaryTransitions transistions;
	private FormSummaryView view;
	private final FormEntryModel model;

	public FormSummaryController(FormEntryModel model) {
		this.model = model;
		view = new FormSummaryView(model);
		view.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command command, Displayable d) {
		if (d == view) {
			if (command == view.CMD_EXIT) {
				transistions.exit();
			} else if (command == view.CMD_SAVE_EXIT) {
				int counter = countUnansweredQuestions(true);
				if (counter > 0) {
					String txt = "There are unanswered compulsory questions and must be completed first to proceed";
					J2MEDisplay.showError("Question Required!", txt);
				} else {
					transistions.saveAndExit(true);
				}
			} else if (command == List.SELECT_COMMAND) {
				transistions.viewForm(view.getFormIndex());
			}
		}
	}

	/**
	 * @param countRequiredOnly
	 *            if true count only the questions that are unanswered and also
	 *            required
	 * @return number of unanswered questions
	 */
	public int countUnansweredQuestions(boolean countRequiredOnly) {
		// TODO - should this include only relevant questions?
		int counter = 0;

		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();
		while (!index.isEndOfFormIndex()) {
			IFormElement element = form.getChild(index);
			if (element instanceof QuestionDef) {
				FormEntryPrompt prompt = model.getQuestionPrompt(index);
				if (countRequiredOnly) {
					if (prompt.isRequired() && prompt.getAnswerValue() == null) {
						counter++;
					}
				} else if (prompt.getAnswerValue() == null) {
					counter++;
				}
			}
			index = form.incrementIndex(index);
		}

		return counter;
	}

	public void setTransitions(FormSummaryState transitions) {
		this.transistions = transitions;
	}

	public void start() {
		view.show();
	}

}
