/**
 * 
 */
package org.javarosa.forms.review.api.transitions;

import org.javarosa.core.api.Transitions;
import org.javarosa.forms.review.util.DataModelDateFilter;

/**
 * @author ctsims
 *
 */
public interface ReviewSelectionStateTransitions extends Transitions {
	public void back();
	public void filterSelected(DataModelDateFilter filter);
}
