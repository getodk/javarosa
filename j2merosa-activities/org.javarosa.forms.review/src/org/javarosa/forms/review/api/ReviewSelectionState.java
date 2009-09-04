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
			//Today
			start = DateUtils.roundDate(new Date());
		} else if(i == 1) {
			//Yesterday
			end = DateUtils.roundDate(new Date());
			start = new Date(end.getTime() - DateUtils.DAY_IN_MS);
		} else if(i == 2) {
			//This Week
			start = getBeginningOfWeek(0);
		} else if(i == 3) {
			//Last Week
			end = getBeginningOfWeek(0);
			start = getBeginningOfWeek(1);	
		}
		
		System.out.println(DateUtils.formatDate(start, DateUtils.FORMAT_HUMAN_READABLE_SHORT) + " -> " + DateUtils.formatDate(end, DateUtils.FORMAT_HUMAN_READABLE_SHORT));
		
		return new DataModelDateFilter(start,end);
	}
	
	private Date getBeginningOfWeek(int weeksago) {
		return DateUtils.getPastPeriodDate(new Date(), "week", "sun", true, true, weeksago);
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
