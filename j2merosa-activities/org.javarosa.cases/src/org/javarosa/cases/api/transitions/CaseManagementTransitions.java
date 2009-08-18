/**
 * 
 */
package org.javarosa.cases.api.transitions;

import org.javarosa.core.api.Transitions;

/**
 * @author ctsims
 *
 */
public interface CaseManagementTransitions extends Transitions{
	public void newCase();
	public void followUpOnCase();
	public void viewReferrals();
	public void viewOpen();
	public void closeCase();
	public void exit();
}
