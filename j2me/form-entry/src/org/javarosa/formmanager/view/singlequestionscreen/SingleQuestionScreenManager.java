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

package org.javarosa.formmanager.view.singlequestionscreen;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquireScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreenFactory;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.List;
import de.enough.polish.util.Locale;

public class SingleQuestionScreenManager extends FramedForm implements IFormEntryView,
		CommandListener, ItemCommandListener {
	private JrFormEntryController controller;
	private FormEntryModel model;

	private SingleQuestionScreen currentQuestionScreen;
	private boolean goingForward;
	private FormViewScreen formView;

	// GUI elements
	public SingleQuestionScreenManager(JrFormEntryController controller) {
		super(controller.getModel().getFormTitle());
		this.controller = controller;
		this.model = controller.getModel();
		this.goingForward = true;
	}

	public SingleQuestionScreen getView(FormIndex qIndex, boolean fromFormView) {
		FormEntryPrompt prompt = model.getQuestionPrompt(qIndex);
		FormEntryCaption[] captionHeirarchy = model.getCaptionHeirarchy(qIndex);
		String groupTitle = null;
		if (captionHeirarchy.length > 1) {
			FormEntryCaption caption = captionHeirarchy[1];
			groupTitle = caption.getShortText();
		}

		currentQuestionScreen = SingleQuestionScreenFactory.getQuestionScreen(
				prompt, groupTitle, fromFormView, goingForward);

		if (model.getLanguages().length > 0) {
			currentQuestionScreen.addLanguageCommands(model.getLanguages());
		}
		currentQuestionScreen.setCommandListener(this);
		currentQuestionScreen.setItemCommandListner(this);
		return currentQuestionScreen;
	}

	public void destroy() {
	}

	public void show() {
		showFormViewScreen();
	}

	private void showFormViewScreen() {
		controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		formView = new FormViewScreen(this.model);
		formView.setCommandListener(this);
	}

	public void refreshView() {
		SingleQuestionScreen view = getView(model.getCurrentFormIndex(),
				this.goingForward);
		controller.setView(view);
	}

	public void commandAction(Command command, Displayable arg1) {
		if (arg1 == formView) {
			formViewCommands(command);
		} else {
			if (command == SingleQuestionScreen.nextItemCommand
					|| command == SingleQuestionScreen.nextCommand) {
				answerQuestion();
			} else if (command == SingleQuestionScreen.previousCommand) {
				this.goingForward = false;
				int event = controller.stepToPreviousEvent();
				processModelEvent(event);
			} else if (command == SingleQuestionScreen.viewAnswersCommand) {
				viewAnswers();
			}

			// TODO: FIXME
			else if ((arg1 instanceof AcquireScreen)) {
				// // handle additional commands for acquring screens
				// AcquireScreen source = (AcquireScreen) arg1;
				// System.out.println("Got event from AcquireScreen");
				// if (command == source.cancelCommand) {
				// AcquiringQuestionScreen questionScreen = source
				// .getQuestionScreen();
				// questionScreen.setCommandListener(this);
				// J2MEDisplay.setView(questionScreen);
				// }
				// } else if (arg1 instanceof AcquiringQuestionScreen) {
				// // handle additional commands for acquring question screens
				// AcquiringQuestionScreen aqQuestionScreen =
				// (AcquiringQuestionScreen) arg1;
				// if (command == aqQuestionScreen.acquireCommand) {
				// J2MEDisplay
				// .setView(aqQuestionScreen.getAcquireScreen(this));
				// }
			} else // should be a command in the language submenu
			{
				String language = null;
				for (int i = 0; i < SingleQuestionScreen.languageCommands.length; i++) {
					if (command == SingleQuestionScreen.languageCommands[i]) {
						language = command.getLabel();
						break;
					}
				}

				if (language != null) {
					controller.setLanguage(language);
					switchViewLanguage();
				} else {
					System.err.println("Unknown command event received ["
							+ command.getLabel() + "]");
				}
			}

		}
	}

	private void viewAnswers() {
		controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		showFormViewScreen();
	}

	private void switchViewLanguage() {
		IAnswerData answer = currentQuestionScreen.getWidgetValue();
		this.goingForward = true;
		controller.answerCurrentQuestion(answer);
		refreshView();
	}

	private void processModelEvent(int event) {
		int nextEvent = -1;
		switch (event) {
		case FormEntryController.BEGINNING_OF_FORM_EVENT:
			if (goingForward)
				nextEvent = controller.stepToNextEvent();
			else {
				viewAnswers();
			}
			break;
		case FormEntryController.END_OF_FORM_EVENT:
			viewAnswers();
			break;
		case FormEntryController.REPEAT_EVENT:
			// TODO
			break;
		case FormEntryController.PROMPT_NEW_REPEAT_EVENT:
			// TODO
			break;
		case FormEntryController.GROUP_EVENT:
			nextEvent = goingForward ? controller.stepToNextEvent()
					: controller.stepToPreviousEvent();
			break;
		case FormEntryController.QUESTION_EVENT:
			refreshView();
			break;
		default:
			break;
		}
		if (nextEvent > 0)
			processModelEvent(nextEvent);
	}

	private void formViewCommands(Command command) {
		if (command == FormViewScreen.backCommand) {
			this.show();
		} else if (command == FormViewScreen.exitNoSaveCommand) {
			// TODO: FIXME
			// controller.exit();
		} else if (command == FormViewScreen.exitSaveCommand) {
			// TODO: FIXME
			// controller.save();
			// controller.exit();
		} else if (command == FormViewScreen.sendCommand) {
			int counter = countUnansweredQuestions(true);
			if (counter > 0) {
				String txt = Locale
						.get("view.sending.CompulsoryQuestionsIncomplete");
				J2MEDisplay.showError("Question Required!", txt);
			} else {
				// TODO: FIXME
				// model.setFormComplete();
				// controller.exit();
			}
		} else if (command == List.SELECT_COMMAND) {
			int i = formView.getSelectedIndex();
			FormIndex b = formView.indexHash.get(i);
			if (!model.isReadonly(b)) {
				controller.jumpToIndex(b);
				this.goingForward = true;
				refreshView();
			} else {
				String txt = Localization.get("view.sending.FormUneditable");
				J2MEDisplay.showError("Cannot Edit Answers!", txt);
			}
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == SingleQuestionScreen.nextItemCommand) {
			answerQuestion();
		}
	}

	private void answerQuestion() {
		IAnswerData answer = currentQuestionScreen.getWidgetValue();
		this.goingForward = true;
		int result = controller.answerCurrentQuestion(answer);
		if (result == FormEntryController.ANSWER_OK) {
			controller.stepToNextEvent();
			refreshView();
		} else if (result == FormEntryController.ANSWER_CONSTRAINT_VIOLATED) {
			J2MEDisplay.showError("Validation failure", model
					.getCurrentQuestionPrompt().getConstraintText());
		} else if (result == FormEntryController.ANSWER_REQUIRED_BUT_EMPTY) {
			String txt = Locale
					.get("view.sending.CompulsoryQuestionIncomplete");
			J2MEDisplay.showError("Question Required", txt);
		}
		int event = controller.stepToNextEvent();
		processModelEvent(event);
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
			FormEntryPrompt prompt = new FormEntryPrompt(form, index);
			if (countRequiredOnly && prompt.isRequired()
					&& prompt.getAnswerValue() == null) {
				counter++;
			} else if (prompt.getAnswerValue() == null) {
				counter++;
			}
			index = form.incrementIndex(index);
		}

		return counter;
	}
}