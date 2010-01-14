/**
 * 
 */
package org.javarosa.formmanager.view.singlequestionscreen.screen;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.FormElementBinding;
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
	 * @param prompt
	 * @param fromFormView
	 * @param goingForward
	 *            - i.e. are we going to the next question in the form (rather
	 *            than the previous one)
	 * @return
	 * @throws IllegalStateException
	 */
	public static SingleQuestionScreen getQuestionScreen(
			FormElementBinding prompt, boolean fromFormView,
			boolean goingForward) throws IllegalStateException {
		SingleQuestionScreen screenToReturn = null;
		int qType = prompt.instanceNode.dataType;
		int contType = ((QuestionDef) prompt.element).getControlType();

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? "OneQPS_Form_Right"
						: "OneQPS_Form_Left");

		switch (contType) {
		case Constants.CONTROL_INPUT:
			switch (qType) {
			case Constants.DATATYPE_TEXT:
			case Constants.DATATYPE_NULL:
			case Constants.DATATYPE_UNSUPPORTED:
				screenToReturn = new TextQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_DATE:
			case Constants.DATATYPE_DATE_TIME:
				screenToReturn = new DateQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_TIME:
				screenToReturn = new TimeQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_INTEGER:
				screenToReturn = new NumericQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_DECIMAL:
				screenToReturn = new DecimalQuestionScreen(prompt, style);

				break;

			case Constants.DATATYPE_BARCODE:
				screenToReturn = new TextQuestionScreen(prompt, style);
				break;

			default:
				screenToReturn = new TextQuestionScreen(prompt, style);

			}
			break;
			
		case Constants.CONTROL_SELECT_ONE:
			screenToReturn = new Select1QuestionScreen(prompt, style);
			break;
			
		case Constants.CONTROL_SELECT_MULTI:
			screenToReturn = new SelectQuestionScreen(prompt, style);
			break;
			
		case Constants.CONTROL_TEXTAREA:
			screenToReturn = new TextQuestionScreen(prompt, style);
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
			FormElementBinding prompt, boolean fromFormView,
			boolean goingForward, IAcquiringService barcodeService) {

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? "OneQPS_Form_Right"
						: "OneQPS_Form_Left");

		return barcodeService.getWidget(prompt, style);

	}

}
