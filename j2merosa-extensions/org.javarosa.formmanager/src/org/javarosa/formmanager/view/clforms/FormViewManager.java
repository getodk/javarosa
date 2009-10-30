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

package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.ServiceRegistry;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormEntryModelListener;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.clforms.acquire.AcquireScreen;
import org.javarosa.formmanager.view.clforms.acquire.AcquiringQuestionScreen;
import org.javarosa.formmanager.view.clforms.acquire.IAcquiringService;
import org.javarosa.formmanager.view.clforms.widgets.DateQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.DecimalQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.NumericQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.Select1QuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.SelectQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TextQuestionWidget;
import org.javarosa.formmanager.view.clforms.widgets.TimeQuestionWidget;
import org.javarosa.j2me.view.J2MEDisplay;

public class FormViewManager implements IFormEntryView, FormEntryModelListener,
		CommandListener, ItemCommandListener {
	private FormEntryController controller;
	private FormEntryModel model;
	private FormViewScreen parent;

	private FormIndex index;
	private FormElementBinding prompt;
	private IAnswerData answer;
	private SingleQuestionScreen widget;
	Gauge progressBar;
	private boolean showFormView;
	private FormViewScreen formView;
	private boolean direction;

	// GUI elements
	public FormViewManager(String formTitle, FormEntryModel model,
			FormEntryController controller) {
		this.model = model;
		this.controller = controller;
		this.parent = new FormViewScreen(model);
		this.showFormView = true;
		model.registerObservable(this);
		// immediately setup question, need to decide if this is the best place
		// to do it
		// this.getView(questionIndex);
		controller.setFormEntryView(this);

	}

	public FormIndex getIndex() {
		index = model.getQuestionIndex();// return index of active question
		return index;
	}

	public void getView(FormIndex qIndex, boolean fromFormView) {
		prompt = new FormElementBinding(null, qIndex, model.getForm());
		// checks question type
		int qType = prompt.instanceNode.dataType;
		int contType = ((QuestionDef) prompt.element).getControlType();

		switch (contType) {
		case Constants.CONTROL_INPUT:
			switch (qType) {
			case Constants.DATATYPE_TEXT:
			case Constants.DATATYPE_NULL:
			case Constants.DATATYPE_UNSUPPORTED:
				// go to TextQuestion Widget
				if (fromFormView == true)
					widget = new TextQuestionWidget(prompt, 'c');
				else if (direction)
					widget = new TextQuestionWidget(prompt, 1);
				else
					widget = new TextQuestionWidget(prompt, "");
				break;
			case Constants.DATATYPE_DATE:
			case Constants.DATATYPE_DATE_TIME:
				// go to DateQuestion Widget
				if (fromFormView == true)
					widget = new DateQuestionWidget(prompt, 'c');// transition
				// must be
				// fromAnswerScreen
				// style
				else if (direction == true)
					widget = new DateQuestionWidget(prompt, 1);// transition =
				// next style
				else
					widget = new DateQuestionWidget(prompt, ""); // transition =
				// back
				// style
				break;
			case Constants.DATATYPE_TIME:
				// go to TimeQuestion Widget
				if (fromFormView == true)
					widget = new TimeQuestionWidget(prompt, 'c');
				else if (direction == true)
					widget = new TimeQuestionWidget(prompt, 1);
				else
					widget = new TimeQuestionWidget(prompt, "");
				break;
			case Constants.DATATYPE_INTEGER:
				if (fromFormView == true)
					widget = new NumericQuestionWidget(prompt, 'c');
				else if (direction == true)
					widget = new NumericQuestionWidget(prompt, 1);
				else
					widget = new NumericQuestionWidget(prompt, "");
				break;
			case Constants.DATATYPE_DECIMAL:
				if (fromFormView == true)
					widget = new DecimalQuestionWidget(prompt, 'c');
				else if (direction == true)
					widget = new DecimalQuestionWidget(prompt, 1);
				else
					widget = new DecimalQuestionWidget(prompt, "");
				break;

			case Constants.DATATYPE_BARCODE:
				try { // is there a service that can acquire a barcode?
					IAcquiringService barcodeService = (IAcquiringService)ServiceRegistry.getService("Barcode Acquiring Service");
					if (fromFormView == true)
						widget = barcodeService.getWidget(prompt, 'c');
					else if (direction)
						widget = barcodeService.getWidget(prompt, 1);
					else
						widget = barcodeService.getWidget(prompt, "");

				} catch (UnavailableServiceException se) {
					widget = null;
				}
				// if not, just use a
				// text widget
				if (widget == null)
					if (fromFormView == true)
						widget = new TextQuestionWidget(prompt, 'c');
					else if (direction)
						widget = new TextQuestionWidget(prompt, 1);
					else
						widget = new TextQuestionWidget(prompt, "");

				break;
			}
			break;
		case Constants.CONTROL_SELECT_ONE:
			// go to SelectQuestion widget
			if (fromFormView == true)
				widget = new Select1QuestionWidget(prompt, 'c');
			else if (direction == true)
				widget = new Select1QuestionWidget(prompt, 1);
			else
				widget = new Select1QuestionWidget(prompt, "");
			break;
		case Constants.CONTROL_SELECT_MULTI:
			// go to SelectQuestion Widget
			if (fromFormView == true)
				widget = new SelectQuestionWidget(prompt, 'c');
			else if (direction == true)
				widget = new SelectQuestionWidget(prompt, 1);
			else
				widget = new SelectQuestionWidget(prompt, "");
			break;
		case Constants.CONTROL_TEXTAREA:
			// go to TextQuestion Widget
			if (fromFormView == true)
				widget = new TextQuestionWidget(prompt, 'c');
			else if (direction == true)
				widget = new TextQuestionWidget(prompt, 1);
			else
				widget = new TextQuestionWidget(prompt, "");
			break;
		default:
			System.out.println("Unsupported type!");
			break;
		}
		widget.setCommandListener(this);
		widget.setItemCommandListner(this);
		controller.setView(widget);
	}

	public void destroy() {
		model.unregisterObservable(this);
	}

	public void show() {
		if (this.showFormView)
			showFormViewScreen();
		else
			getView(getIndex(), this.showFormView);// refresh view
	}

	private void showFormViewScreen() {
		model.setQuestionIndex(FormIndex.createBeginningOfFormIndex());
		formView = new FormViewScreen(this.model);
		formView.setCommandListener(this);
		controller.setView(formView);
	}

	public void refreshView() {
		getView(getIndex(), this.showFormView);// refresh view
	}

	public void formComplete() {
		if (!model.isReadOnly()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
			}

			controller.save();// always save form
			controller.exit();
		}
	}

	public void questionIndexChanged(FormIndex questionIndex) {
		if (questionIndex.isInForm())
			getView(getIndex(), this.showFormView);// refresh view
	}

	public void saveStateChanged(int instanceID, boolean dirty) {
		// TODO Auto-generated method stub

	}

	public void commandAction(Command command, Displayable arg1) {
		if (arg1 == formView) {
			if (command == FormViewScreen.backCommand) {
				this.show();
			} else if (command == FormViewScreen.exitNoSaveCommand) {
				controller.exit();
			} else if (command == FormViewScreen.exitSaveCommand) {
				controller.save();
				controller.exit();
			} else if (command == FormViewScreen.sendCommand) {
				// check if all required questions are complete
				int counter = model.countUnansweredQuestions(true);
				
				if (counter > 0) {
					// show alert
					String txt = "There are unanswered compulsory questions and must be completed first to proceed";
					J2MEDisplay.showError("Question Required!", txt);
				} else
					model.setFormComplete();
				// controller.exit();
			} else if (command == List.SELECT_COMMAND) {
				if (!model.isReadOnly()) {
					int i = formView.getSelectedIndex();
					FormIndex b = formView.indexHash.get(i);
					controller.selectQuestion(b);
					this.showFormView = false;
				} else {
					String txt = Localization.get(
							"view.sending.FormUneditable");
					J2MEDisplay.showError("Cannot Edit Answers!", txt);
				}
			}

		} else {
			if (command == SingleQuestionScreen.nextItemCommand
					|| command == SingleQuestionScreen.nextCommand) {
				answer = widget.getWidgetValue();

				int result = controller.questionAnswered(this.prompt,
						this.answer);
				if (result == controller.QUESTION_CONSTRAINT_VIOLATED) {
//					System.out.println("answer validation constraint violated");
//					TODO:   String txt = Locale.get(
//							"view.sending.CompulsoryQuestionsIncomplete");
					
					String txt = "Validation failure: data is not of the correct format.";
					
					J2MEDisplay.showError("Question Required!", txt);
				} else if (result == controller.QUESTION_REQUIRED_BUT_EMPTY) {
//					String txt = Locale.get(
//							"view.sending.CompulsoryQuestionsIncomplete");
					String txt = "This question is compulsory. You must answer it.";
					J2MEDisplay.showError("Question Required!", txt);
				}
		
			} else if (command == SingleQuestionScreen.previousCommand) {
				direction = false;
				controller.stepQuestion(direction);

			} else if (command == SingleQuestionScreen.viewAnswersCommand) {
				controller.selectQuestion(FormIndex
						.createBeginningOfFormIndex());
				this.showFormView = true;
				showFormViewScreen();
			} else if (command == SingleQuestionScreen.viewAnswersCommand) {
				controller.selectQuestion(FormIndex
						.createBeginningOfFormIndex());
				this.showFormView = true;
				showFormViewScreen();
			} else if ((arg1 instanceof AcquireScreen)) {
				// handle additional commands for acquring screens
				AcquireScreen source = (AcquireScreen) arg1;
				System.out.println("Got event from AcquireScreen");
				if (command == source.cancelCommand) {
					AcquiringQuestionScreen questionScreen = source
							.getQuestionScreen();
					questionScreen.setCommandListener(this);
					controller.setView(questionScreen);
				}
			} else if (arg1 instanceof AcquiringQuestionScreen) {
				// handle additional commands for acquring question screens
				AcquiringQuestionScreen aqQuestionScreen = (AcquiringQuestionScreen) arg1;
				System.out
						.println("Got event from AcquiringSingleQuestionScreen");
				if (command == aqQuestionScreen.acquireCommand) {
					controller.setView(aqQuestionScreen.getAcquireScreen(this));
				}
			}

		}
	}

	
	public void commandAction(Command c, Item item) {
		if (c == SingleQuestionScreen.nextItemCommand) {
			answer = widget.getWidgetValue();
			controller.questionAnswered(this.prompt, answer);// store answers
			refreshView();

		}
	}

	public boolean isShowOverView() {
		return showFormView;
	}

	public void setShowOverView(boolean showOverView) {
		this.showFormView = showOverView;
	}

	public void startOfForm() {
		// TODO Auto-generated method stub

	}
}