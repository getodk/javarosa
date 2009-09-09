/**
 * 
 */
package org.javarosa.cases.api;

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
public class CaseManagementState implements State<CaseManagementTransitions>, CommandListener {

	
    public static final String NEW = Localization.get("menu.NewCase");
    public static final String FOLLOWUP = Localization.get("menu.FollowUp");
    public static String REFERRAL = Localization.get("menu.Referral");
    public static final String VIEW_OPEN = Localization.get("menu.ViewOpen");
    public static final String RESOLVE = Localization.get("menu.Resolve");
    
    ICaseType type;
	CaseManagementScreen view;
	CaseManagementTransitions transitions;

	private void configView() {
		view = new CaseManagementScreen("Select Action");
		view.insert(0,NEW,null);
		view.insert(1,FOLLOWUP,null);
		view.insert(2,REFERRAL,null);
		view.insert(3,RESOLVE,null);
		view.insert(4,VIEW_OPEN,null);		
	}

	public CaseManagementState(ICaseType type) {
		this.type = type;
		
		REFERRAL = Localization.get("menu.Referral",new String[] {String.valueOf(PatientReferralUtil.getNumberOfOpenReferralsByType(type.getCaseTypeId()))} );
		view = new CaseManagementScreen("Select Action");
		configView();
		view.setCommandListener(this);
	}
	
	public void enter(CaseManagementTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.getDisplay().setCurrent(view);
	}

	public void commandAction(Command c, Displayable arg1) {
		if(c.equals(List.SELECT_COMMAND)) {
			switch(view.getSelectedIndex()) {
				case 0:
					transitions.newCase();
					break;
				case 1:
					transitions.followUpOnCase();
					break;
				case 2:
					transitions.viewReferrals();
					break;
				case 3:
					transitions.closeCase();
					break;
				case 4:
					transitions.viewOpen();
					break;
			}
		}
		else if(c.equals(CaseManagementScreen.BACK)) {
			transitions.exit();
		}
	}


}
