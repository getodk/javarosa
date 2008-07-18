package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.StringItem;

import org.javarosa.clforms.api.Prompt;
import org.javarosa.core.model.QuestionData;

import de.enough.polish.ui.Container;

/**
 * Default read-only view of a question: a single frame with question prompt (abbreviated) on the left/top and 
 * question answer in readable-text form on the right/bottom.
 */
public class CollapsedWidget implements IWidgetStyle {
	private StringItem prompt;
	private StringItem answer;

	public CollapsedWidget () {
		reset();
	}

	/**
	 * TODO: be smart about layout/wrapping; take into account lengths of prompt and answer for optimum
	 * use of vertical screen space
	 */

	public void initWidget (Prompt question, Container c) {
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

	public void refreshWidget (Prompt question, QuestionData data, int changeFlags) {
		prompt.setText(question.getShortText());
		answer.setText(data.getDisplayText());
	}

	public void reset () {
		prompt = null;
		answer = null;
	}
}
