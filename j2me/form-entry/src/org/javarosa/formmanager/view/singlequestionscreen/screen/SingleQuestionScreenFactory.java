/**
 * 
 */
package org.javarosa.formmanager.view.singlequestionscreen.screen;

import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.FormMultimediaController;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.IAcquiringService;
import org.javarosa.formmanager.view.widgets.IWidgetStyleEditable;
import org.javarosa.formmanager.view.widgets.WidgetFactory;

import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

/**
 * @author melissa
 * 
 */
public class SingleQuestionScreenFactory {
	
	WidgetFactory widgetFactory;
	FormMultimediaController mediacontroller;
	JrFormEntryController fec;
	
	public SingleQuestionScreenFactory(JrFormEntryController fec, FormMultimediaController controller) {
		this(fec, controller, new WidgetFactory(false));
	}
	
	public SingleQuestionScreenFactory(JrFormEntryController fec, FormMultimediaController controller, WidgetFactory factory) {
		this.widgetFactory = factory;
		this.mediacontroller = controller;
		this.fec = fec;
	}

	/**
	 * Return an appropriate SingleQuestionScreen to display the prompt
	 * 
	 * @param binding
	 * @param fromFormView
	 * @param goingForward
	 *            - i.e. are we going to the next question in the form (rather
	 *            than the previous one)
	 * @param quickEntry Whether the UI for entry should be streamlined (auto select/move, etc).
	 * @return
	 * @throws IllegalStateException
	 */
	public SingleQuestionScreen getQuestionScreen(
			FormEntryPrompt prompt, String groupTitle, boolean fromFormView,
			boolean goingForward) throws IllegalStateException {
		SingleQuestionScreen screenToReturn = null;
		int qType = prompt.getDataType();
		int contType = prompt.getControlType();

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_RIGHT
						: org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_LEFT);

		
		IWidgetStyleEditable widget = widgetFactory.getWidget(contType, qType, prompt.getAppearanceHint());
		
		if(widget == null) {
			throw new IllegalStateException("No appropriate screen to render question");
		}
		
		widget.registerMultimediaController(mediacontroller);
		screenToReturn = new SingleQuestionScreen(prompt, groupTitle, widget, fec, style);
		
		prompt.register(screenToReturn);

		return screenToReturn;
	}

	/**
	 * Get a SingleQuestionScreen for acquiring data by methods other than
	 * direct user input (e.g. a barcode)
	 * 
	 * @param prompt
	 * @param fromFormView
	 * @param goingForward
	 *            - i.e. are we going to the next question in the form (rather
	 *            than the previous one)
	 * @param barcodeService
	 * @return
	 */
	public SingleQuestionScreen getQuestionScreen(
			FormEntryPrompt prompt, String groupTitle, boolean fromFormView,
			boolean goingForward, IAcquiringService barcodeService) {

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_RIGHT
						: org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_LEFT);

		return barcodeService.getWidget(prompt, style);

	}

}
