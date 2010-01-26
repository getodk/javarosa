/**
 * 
 */
package org.javarosa.cases.api;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.cases.api.transitions.CaseManagementTransitions;
import org.javarosa.cases.util.ICaseType;
import org.javarosa.cases.view.CaseManagementScreen;
import org.javarosa.chsreferral.util.PatientReferralUtil;
import org.javarosa.core.api.State;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class CaseManagementController implements CommandListener {
    public static final String NEW = "menu.NewCase";
    public static final String FOLLOWUP = "menu.FollowUp";
    public static final String REFERRAL = "menu.Referral";
    public static final String VIEW_OPEN = "menu.ViewOpen";
    public static final String RESOLVE = "menu.Resolve";
    
    ICaseType type;
	CaseManagementScreen view;
	CaseManagementTransitions transitions;

	Vector indexMapping = new Vector();
	
	public CaseManagementController(ICaseType type) {
		this.type = type;
		
		view = new CaseManagementScreen("Select Action");
		configView();
		view.setCommandListener(this);
	}
	
	public void setTransitions (CaseManagementTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.setView(view);
	}
	
	private void configView() {
		int[] order = type.getActionListing();
		if (order != null) {
			for (int i = 0; i < order.length; i++) {
				addOption(order[i]);
			}
		} else {
			addOption(ICaseType.ACTION_NEW);
			addOption(ICaseType.ACTION_FOLLOWUP);
			addOption(ICaseType.ACTION_REFERRALS);
			addOption(ICaseType.ACTION_CLOSE);
			addOption(ICaseType.ACTION_BROWSE);		
		}
	}

	private String captionForAction (int action) {
		Hashtable capov = type.getCaptionOverrides();
		String locKey = (String)capov.get(new Integer(action));
		if (locKey == null) {		
			switch (action) {
			case ICaseType.ACTION_NEW: locKey = NEW; break;
			case ICaseType.ACTION_FOLLOWUP: locKey = FOLLOWUP; break;
			case ICaseType.ACTION_REFERRALS: locKey = REFERRAL; break;
			case ICaseType.ACTION_CLOSE: locKey = RESOLVE; break;
			case ICaseType.ACTION_BROWSE: locKey = VIEW_OPEN; break;
			}
		}

		String caption;
		if (action == ICaseType.ACTION_REFERRALS) {
			caption = Localization.get(locKey, new String[] {String.valueOf(PatientReferralUtil.getNumberOfOpenReferralsByType(null))});			
		} else {
			caption = Localization.get(locKey);
		}
		return caption;
	}
	
	private void addOption (int action) {
		String caption = captionForAction(action);
		indexMapping.addElement(new Integer(action));
		view.append(caption, null);
	}

	public void commandAction(Command c, Displayable arg1) {
		if(c.equals(List.SELECT_COMMAND)) {
			int action = ((Integer)indexMapping.elementAt(view.getSelectedIndex())).intValue();
			
			switch(action) {
				case ICaseType.ACTION_NEW:
					transitions.newCase();
					break;
				case ICaseType.ACTION_FOLLOWUP:
					transitions.followUpOnCase();
					break;
				case ICaseType.ACTION_REFERRALS:
					transitions.viewReferrals();
					break;
				case ICaseType.ACTION_CLOSE:
					transitions.closeCase();
					break;
				case ICaseType.ACTION_BROWSE:
					transitions.viewOpen();
					break;
			}
		}
		else if(c.equals(CaseManagementScreen.BACK)) {
			transitions.exit();
		}
	}


}
