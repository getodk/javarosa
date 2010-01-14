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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquireScreen;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquiringQuestionScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreen;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreenFactory;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.util.Locale;

public class SingleQuestionScreenManager implements IFormEntryView,
		CommandListener, ItemCommandListener {
	private FormEntryController controller;
	private FormEntryModel model;

	private FormElementBinding binding;
	private SingleQuestionScreen currentQuestionScreen;
	private boolean goingForward;
	private FormViewScreen formView;

	// GUI elements
	public SingleQuestionScreenManager(String formTitle,
			FormEntryController controller) {
		this.controller = controller;
		this.model = controller.getModel();
		this.goingForward = true;
	}

	public SingleQuestionScreen getView(FormIndex qIndex, boolean fromFormView) {
		binding = new FormElementBinding(null, qIndex, model.getForm());
		if (((QuestionDef) binding.element).getControlType() == Constants.DATATYPE_BARCODE) {
			// TODO: FIXME
			// try { // is there a service that can acquire a barcode?
			// IAcquiringService barcodeService = (IAcquiringService) controller
			// .getDataCaptureService("singlequestionscreen-barcode");			
			//
			// currentQuestionScreen = SingleQuestionScreenFactory
			// .getQuestionScreen(prompt, fromFormView, goingForward,
			// barcodeService);
			//
			// } catch (UnavailableServiceException se) {
			// // otherwise just get whatever else can handle the question type
			// currentQuestionScreen = SingleQuestionScreenFactory
			// .getQuestionScreen(prompt, fromFormView, goingForward);
			// }

		} else {
			currentQuestionScreen = SingleQuestionScreenFactory
					.getQuestionScreen(binding, fromFormView, goingForward);
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
		model.setQuestionIndex(FormIndex.createBeginningOfFormIndex());
		formView = new FormViewScreen(this.model);
		formView.setCommandListener(this);
		J2MEDisplay.setView(formView);
	}

	public void refreshView() {
		SingleQuestionScreen view = getView(model.getCurrentFormIndex(),
				this.goingForward);
		J2MEDisplay.setView(view);
	}

	// public void formComplete() {
	// if (!model.isReadOnly()) {
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException ie) {
	// }
	//
	// controller.save();// always save form
	// controller.exit();
	// }
	// }

	// public void questionIndexChanged(FormIndex questionIndex) {
	// if (questionIndex.isInForm())
	// getView(model.getCurrentFormIndex(), this.showFormView);// refresh
	// // view
	// }

	public void saveStateChanged(int instanceID, boolean dirty) {
	}

	public void commandAction(Command command, Displayable arg1) {
		if (arg1 == formView) {
			extracted(command);
		} else {
			if (command == SingleQuestionScreen.nextItemCommand
					|| command == SingleQuestionScreen.nextCommand) {
				IAnswerData answer = currentQuestionScreen.getWidgetValue();
				this.goingForward = true;
				int result = controller.questionAnswered(answer);
				if (result == FormEntryController.QUESTION_OK) {
					controller.stepToNextEvent();
					refreshView();
				} else if (result == FormEntryController.QUESTION_CONSTRAINT_VIOLATED) {
					J2MEDisplay.showError("Validation failure", model
							.getQuestionPrompt().getConstraintText());
				} else if (result == FormEntryController.QUESTION_REQUIRED_BUT_EMPTY) {
					String txt = Locale
							.get("view.sending.CompulsoryQuestionIncomplete");
					J2MEDisplay.showError("Question Required", txt);
				}
			} else if (command == SingleQuestionScreen.previousCommand) {
				this.goingForward = false;
				controller.stepPreviousEvent();
				refreshView();
			} else if (command == SingleQuestionScreen.viewAnswersCommand) {
				controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
				showFormViewScreen();
			} else if ((arg1 instanceof AcquireScreen)) {
				// handle additional commands for acquring screens
				AcquireScreen source = (AcquireScreen) arg1;
				System.out.println("Got event from AcquireScreen");
				if (command == source.cancelCommand) {
					AcquiringQuestionScreen questionScreen = source
							.getQuestionScreen();
					questionScreen.setCommandListener(this);
					J2MEDisplay.setView(questionScreen);
				}
			} else if (arg1 instanceof AcquiringQuestionScreen) {
				// handle additional commands for acquring question screens
				AcquiringQuestionScreen aqQuestionScreen = (AcquiringQuestionScreen) arg1;
				if (command == aqQuestionScreen.acquireCommand) {
					J2MEDisplay
							.setView(aqQuestionScreen.getAcquireScreen(this));
				}
			}

		}
	}

	private void extracted(Command command) {
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
			int counter = model.countUnansweredQuestions(true);
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
				String txt = Localization
						.get("view.sending.FormUneditable");
				J2MEDisplay.showError("Cannot Edit Answers!", txt);
			}
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == SingleQuestionScreen.nextItemCommand) {
			IAnswerData answer = currentQuestionScreen.getWidgetValue();
			controller.questionAnswered(answer);// store answers
			refreshView();
		}
	}
}