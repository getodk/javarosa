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
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquireScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.NewRepeatScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreenFactory;
import org.javarosa.formmanager.view.summary.FormSummaryState;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledPCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.FramedForm;

public class SingleQuestionView extends FramedForm implements IFormEntryView,
		HandledPCommandListener {
	private JrFormEntryController controller;
	private FormEntryModel model;

	private SingleQuestionScreen currentQuestionScreen;
	private boolean goingForward;
	private NewRepeatScreen repeatScreen;

	// GUI elements
	public SingleQuestionView(JrFormEntryController controller) {
		super(controller.getModel().getFormTitle());
		this.controller = controller;
		this.model = controller.getModel();
		this.goingForward = true;
	}

	public SingleQuestionScreen getView(FormEntryPrompt prompt,
			boolean fromFormView) {

		FormEntryCaption[] captionHierarchy = model.getCaptionHierarchy(prompt
				.getIndex());
		String groupTitle = "";
		if (captionHierarchy.length > 1) {
			int instanceIndex = prompt.getIndex().getInstanceIndex();
			int captionCount = 0;
			for (FormEntryCaption caption : captionHierarchy) {
				captionCount++;
				groupTitle += caption.getLongText(null);

				if ((caption.getIndex().getInstanceIndex() > -1)
						&& (captionCount < captionHierarchy.length))
					groupTitle += " #" + (caption.getMultiplicity() + 1);

				groupTitle += ": ";
			}
			if (groupTitle.endsWith(": "))
				groupTitle = groupTitle.substring(0, groupTitle.length() - 2);
		}
		currentQuestionScreen = SingleQuestionScreenFactory.getQuestionScreen(
				prompt, groupTitle, fromFormView, goingForward);

		if (model.getLanguages() != null && model.getLanguages().length > 0) {
			currentQuestionScreen.addLanguageCommands(model.getLanguages());
		}
		currentQuestionScreen.setCommandListener(this);
		return currentQuestionScreen;
	}

	public void destroy() {
	}

	public void show() {
		showFormSummary();
	}

	public void show(FormIndex index) {
		controller.jumpToIndex(index);
		refreshView();
	}

	private void showFormSummary() {
		controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		FormSummaryState summaryState = new FormSummaryState(controller);
		summaryState.start();
	}

	public void refreshView() {
		if (model.getCurrentEvent() == FormEntryController.EVENT_QUESTION) {
			FormEntryPrompt prompt = model.getCurrentQuestionPrompt();
			SingleQuestionScreen view = getView(prompt, this.goingForward);
			J2MEDisplay.setView(view);
		}
		else if (model.getCurrentEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
			FormEntryCaption[] hierachy = model.getCaptionHierarchy(model
					.getCurrentFormIndex());
			repeatScreen = new NewRepeatScreen(
					"Add "
							+ (model.getCurrentFormIndex()
									.getElementMultiplicity() == 0 ? "a new "
									: "another ")
							+ hierachy[hierachy.length - 1].getLongText(null) + "?");
			repeatScreen.setCommandListener(this);
			J2MEDisplay.setView(repeatScreen);
		}
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command command, Displayable arg1) {
		if (arg1 == repeatScreen) {
			if (command == NewRepeatScreen.yesCommand) {
				controller.newRepeat(model.getCurrentFormIndex());
				controller.stepToNextEvent();
				refreshView();
			} else {
				processModelEvent(controller.stepToNextEvent());
			}
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
						String label = command.getLabel(); // has form language
						// > mylanguage
						int sep = label.indexOf(">");
						language = label.substring(sep + 1, label.length())
								.trim();
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
		showFormSummary();
	}

	private void switchViewLanguage() {
		IAnswerData answer = currentQuestionScreen.getWidgetValue();
		this.goingForward = true;
		controller.answerQuestion(controller.getModel().getCurrentFormIndex(),
				answer);
		refreshView();
	}

	private void processModelEvent(int event) {
		int nextEvent = -1;
		switch (event) {
		case FormEntryController.EVENT_BEGINNING_OF_FORM:
			if (goingForward)
				nextEvent = controller.stepToNextEvent();
			else {
				viewAnswers();
			}
			break;
		case FormEntryController.EVENT_END_OF_FORM:
			viewAnswers();
			break;
		case FormEntryController.EVENT_REPEAT:
		case FormEntryController.EVENT_GROUP:
			nextEvent = goingForward ? controller.stepToNextEvent()
					: controller.stepToPreviousEvent();
			break;
		case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
			refreshView();
			break;
		case FormEntryController.EVENT_QUESTION:
			refreshView();
			break;
		default:
			break;
		}
		if (nextEvent > 0)
			processModelEvent(nextEvent);
	}

	private void answerQuestion() {
		IAnswerData answer = currentQuestionScreen.getWidgetValue();
		this.goingForward = true;
		int result = controller.answerQuestion(controller.getModel()
				.getCurrentFormIndex(), answer);
		if (result == FormEntryController.ANSWER_OK) {
			int event = controller.stepToNextEvent();
			processModelEvent(event);
		} else if (result == FormEntryController.ANSWER_CONSTRAINT_VIOLATED) {
			J2MEDisplay.showError("Validation failure", model
					.getCurrentQuestionPrompt().getConstraintText());
		} else if (result == FormEntryController.ANSWER_REQUIRED_BUT_EMPTY) {
			String txt = Localization
					.get("formview.CompulsoryQuestionIncomplete");
			J2MEDisplay.showError("Question Required", txt);
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
				if (countRequiredOnly && prompt.isRequired()
						&& prompt.getAnswerValue() == null) {
					counter++;
				} else if (prompt.getAnswerValue() == null) {
					counter++;
				}
			}
			index = form.incrementIndex(index);
		}

		return counter;
	}
}