package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.StringItem;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;

/**
 * Default read-only view of a question: a single frame with question QuestionDef (abbreviated) on the left/top and 
 * question answer in readable-text form on the right/bottom.
 */
public class CollapsedWidget implements IWidgetStyle {
	private StringItem prompt;
	private StringItem answer;

	public CollapsedWidget () {
		reset();
	}

	/**
	 * TODO: be smart about layout/wrapping; take into account lengths of QuestionDef and answer for optimum
	 * use of vertical screen space
	 */

	public void initWidget (QuestionDef question, Container c) {
		//#style split
		c.setStyle(); //polish pre-processing turns this into a valid method call

		//#style splitleft
		prompt = new StringItem(null, null);
		//#style splitright
		answer = new StringItem(null, null); 

		//polish has a quirk where it really wants to impose the parent styling onto the first item in the
		//container, even if you explicitly override it with a new style. this null item takes the fall
		c.add(new StringItem(null, null));
		c.add(prompt);
		c.add(answer);
	}

	public void refreshWidget (QuestionDef question, IAnswerData data, int changeFlags) {
		prompt.setText(question.getShortText());
		if (data != null) {
			answer.setText(data.getDisplayText());
		}
	}

	public void reset () {
		prompt = null;
		answer = null;
	}
}
