package org.javarosa.cases.api;

import org.javarosa.cases.api.transitions.CaseManagementTransitions;
import org.javarosa.cases.util.ICaseType;
import org.javarosa.core.api.State;

public abstract class CaseManagementState implements CaseManagementTransitions, State {
	protected ICaseType caseType;
	
	public CaseManagementState (ICaseType caseType) {
		this.caseType = caseType;
	}
	
	public void start () {
		CaseManagementController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected CaseManagementController getController () {
		return new CaseManagementController(caseType);
	}
}
