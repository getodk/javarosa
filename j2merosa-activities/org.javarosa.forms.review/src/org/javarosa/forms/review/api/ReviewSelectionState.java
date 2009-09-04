/**
 * 
 */
package org.javarosa.forms.review.api;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.api.State;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.forms.review.api.transitions.ReviewSelectionStateTransitions;
import org.javarosa.forms.review.util.DataModelDateFilter;
import org.javarosa.j2me.view.J2MEDisplay;


/**
 * @author ctsims
 *
 */
public class ReviewSelectionState implements State<ReviewSelectionStateTransitions>, CommandListener {

	private static Command BACK = new Command("Back", Command.BACK, 1);
	
	ReviewSelectionStateTransitions transitions;
	List options;

	public void enter(ReviewSelectionStateTransitions transitions) {
		this.transitions = transitions;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start() {
		startDisplay();
	}
	
	private DataModelDateFilter getFilterForOption(int i) {
		Date start = new Date(0);
		Date end = new Date();
		
		if(i == 0) {
			start = DateUtils.roundDate(new Date());
		} else if(i == 1) {
			end = DateUtils.roundDate(new Date());
			start = new Date(end.getTime() - DateUtils.DAY_IN_MS);
		} else if(i == 2) {
			start = getBeginningOfWeek();
		} else if(i == 3) {
			end = getBeginningOfWeek();
			start = new Date(end.getTime() - DateUtils.DAY_IN_MS*7);
		}
		
		return new DataModelDateFilter(start,end);
	}
	
	private Date getBeginningOfWeek() {
		Calendar c = Calendar.getInstance();
		int dow = c.get(Calendar.DAY_OF_WEEK);
		int dom = c.get(Calendar.DAY_OF_MONTH);
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		c.set(Calendar.DAY_OF_MONTH, dom - (dow - 1));
		return DateUtils.roundDate(c.getTime());

	}

	private void startDisplay() {
		options = new List(Localization.get("formreview.period.select"), List.IMPLICIT);
		options.insert(0,Localization.get("formreview.period.today"),null);
		options.insert(1,Localization.get("formreview.period.yesterday"),null);
		options.insert(2,Localization.get("formreview.period.week.this"),null);
		options.insert(3,Localization.get("formreview.period.week.last"),null);
		//options.addCommand(List.SELECT_COMMAND);
		options.addCommand(BACK);
		
		options.setCommandListener(this);
		J2MEDisplay.setView(options);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == BACK ) {
			transitions.back();
		} else {
			int index = options.getSelectedIndex();
			if(index == -1) {
				System.out.println("How did we do this???");
			} else {
				transitions.filterSelected(getFilterForOption(index));
			}
		}
	}
}
