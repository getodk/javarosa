/**
 * 
 */
package org.javarosa.cases.activity;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.cases.view.CaseManagementScreen;
import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseManagementActivity implements IActivity, CommandListener {
	
	Context context;
	IShell shell;
	
	CaseManagementScreen view;
	
	// Clayton Sims - Mar 19, 2009 : I'm really not a fan of how this is done. Should
	// be refactored at some point.
	public static final String NEW = "New Case";
	public static final String FOLLOWUP = "Follow Up on Existing Case";
	public static final String VIEW_OPEN = "View Open Cases";
	public static final String RESOLVE = "Resolve Case";

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		start(globalContext);
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
		view = new CaseManagementScreen("Select Action");
		configView();
		view.setCommandListener(this);
		shell.setDisplay(this,view);
	}
	
	private void configView() {
		view.append(NEW, null);
		view.append(FOLLOWUP, null);
		view.append(VIEW_OPEN, null);
		view.append(RESOLVE, null);
	}

	public void commandAction(Command c, Displayable arg1) {
		if(c.equals(CaseManagementScreen.SELECT_COMMAND)) {
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(Constants.RETURN_ARG_KEY, view.getString(view.getSelectedIndex()));
			shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
		}
	}

}
