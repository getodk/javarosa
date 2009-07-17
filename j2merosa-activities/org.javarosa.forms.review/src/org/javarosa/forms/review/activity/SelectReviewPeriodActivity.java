/**
 * 
 */
package org.javarosa.forms.review.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.forms.review.util.DataModelDateFilter;

/**
 * @author ctsims
 *
 */
public class SelectReviewPeriodActivity implements IActivity, CommandListener {
	
	private static Command BACK = new Command("Back", Command.BACK, 1);
	
	IShell shell;
	
	List options;
	
	Context context;
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("Command annotation is unsupported by the Select Review Period Activity");
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		this.context.mergeInContext(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		options = null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		//nothing
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		startDisplay();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = context;
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
		options = new List(JavaRosaServiceProvider.instance().localize("formreview.period.select"), List.IMPLICIT);
		options.insert(0,JavaRosaServiceProvider.instance().localize("formreview.period.today"),null);
		options.insert(1,JavaRosaServiceProvider.instance().localize("formreview.period.yesterday"),null);
		options.insert(2,JavaRosaServiceProvider.instance().localize("formreview.period.week.this"),null);
		options.insert(3,JavaRosaServiceProvider.instance().localize("formreview.period.week.last"),null);
		//options.addCommand(List.SELECT_COMMAND);
		options.addCommand(BACK);
		
		options.setCommandListener(this);
		shell.setDisplay(this, new IView() {
			public Object getScreenObject() {
				// TODO Auto-generated method stub
				return options;
			}
			
		});
	}

	public void commandAction(Command c, Displayable d) {
		Hashtable returnArgs = new Hashtable();
		if(c == BACK ) {
			shell.returnFromActivity(this,Constants.ACTIVITY_CANCEL, returnArgs);
		} else {
			int index = options.getSelectedIndex();
			if(index == -1) {
				System.out.println("How did we do this???");
			} else {
				returnArgs.put(Constants.RETURN_ARG_KEY, getFilterForOption(index));
				shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
			}
		}
	}
}
