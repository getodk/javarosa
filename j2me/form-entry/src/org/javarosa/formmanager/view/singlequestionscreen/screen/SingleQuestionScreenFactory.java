/**
 * 
 */
package org.javarosa.formmanager.view.singlequestionscreen.screen;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.IAcquiringService;

import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

/**
 * @author melissa
 * 
 */
public class SingleQuestionScreenFactory {

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
	public static SingleQuestionScreen getQuestionScreen(
			FormEntryPrompt prompt, String groupTitle, boolean fromFormView,
			boolean goingForward, boolean quickEntry) throws IllegalStateException {
		SingleQuestionScreen screenToReturn = null;
		int qType = prompt.getDataType();
		int contType = prompt.getControlType();

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_RIGHT
						: org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_LEFT);

		switch (contType) {
		case Constants.CONTROL_INPUT:
			switch (qType) {
			case Constants.DATATYPE_TEXT:
			case Constants.DATATYPE_NULL:
			case Constants.DATATYPE_UNSUPPORTED:
				screenToReturn = new TextQuestionScreen(prompt, groupTitle,
						style);

				break;
			case Constants.DATATYPE_DATE:
			case Constants.DATATYPE_DATE_TIME:
				screenToReturn = new DateQuestionScreen(prompt, groupTitle,
						style);

				break;
			case Constants.DATATYPE_TIME:
				screenToReturn = new TimeQuestionScreen(prompt, groupTitle,
						style);

				break;
			case Constants.DATATYPE_INTEGER:
				screenToReturn = new NumericQuestionScreen(prompt, groupTitle,
						style, new IntegerData());

				break;
			case Constants.DATATYPE_LONG:
				screenToReturn = new DecimalQuestionScreen(prompt, groupTitle,
						style, new LongData());

				break;
			case Constants.DATATYPE_DECIMAL:
				screenToReturn = new DecimalQuestionScreen(prompt, groupTitle,
						style, new DecimalData());

				break;

			case Constants.DATATYPE_BARCODE:
				screenToReturn = new TextQuestionScreen(prompt, groupTitle,
						style);
				break;
				
			case Constants.DATATYPE_GEOPOINT:
				screenToReturn = new LocationQuestionScreen(prompt, groupTitle,
						style);
				break;

			default:
				screenToReturn = new TextQuestionScreen(prompt, groupTitle,
						style);

			}
			break;

		case Constants.CONTROL_SELECT_ONE:
			screenToReturn = new SelectOneQuestionScreen(prompt, groupTitle,style, quickEntry);
			break;

		case Constants.CONTROL_SELECT_MULTI:
			screenToReturn = new SelectMultiQuestionScreen(prompt, groupTitle,
					style);
			break;

		case Constants.CONTROL_TEXTAREA:
			screenToReturn = new TextQuestionScreen(prompt, groupTitle, style);
			break;
			
		case Constants.CONTROL_TRIGGER:
			screenToReturn = new TriggerQuestionScreen(prompt, groupTitle, style);
			break;

		default:
			throw new IllegalStateException(
					"No appropriate screen to render question");

		}
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
	public static SingleQuestionScreen getQuestionScreen(
			FormEntryPrompt prompt, String groupTitle, boolean fromFormView,
			boolean goingForward, IAcquiringService barcodeService) {

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_RIGHT
						: org.javarosa.formmanager.view.singlequestionscreen.Constants.STYLE_TRANSITION_LEFT);

		return barcodeService.getWidget(prompt, style);

	}

}
