package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.formmanager.model.temp.*;
import de.enough.polish.ui.*;

/**
 * Strategy object that knows how to build and maintain a widget that contains a view of a single form question. This interface
 * only supports read-only views; there is no provision to get data from the user back out (see IWidgetStyleEditable).
 */
public interface IWidgetStyle {
	/**
	 * Initialize the structure of the widget, agnostic of the question's current locale and data value. This called is
	 * immediately followed by a call to refreshWidget(). This method should save off any references to GUI object that
	 * are necessary to update the widget later in refreshWidget(). If the high-level structure of the widget varies
	 * dynamically, the reference to 'c' itself should be saved.
	 * 
	 * @param question question object this widget represents
	 * @param c top-level container item of the widget, to which GUI items are added. 'c' may also be styled.
	 */
	void initWidget (Prompt question, Container c);
	
	/**
	 * Refresh the widget in response to a change in the question, such as locale, data value, etc. Will be called
	 * immediately after initWidget().
	 * 
	 * @param question question object this widget represents
	 * @param data representation of the question's current data value
	 * @param changeFlags bitmap represent what specifically has changed in the question (see QuestionStateListener)
	 */
	void refreshWidget (Prompt question, QuestionData data, int changeFlags);
	
	/**
	 * Erase all state associated with this widget. If this widget is to be used again, initWidget() will be called again
	 * first.
	 */
	void reset ();
}
