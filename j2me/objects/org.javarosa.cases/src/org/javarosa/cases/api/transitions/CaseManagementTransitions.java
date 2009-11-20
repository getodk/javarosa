/**
 * 
 */
package org.javarosa.cases.api.transitions;


/**
 * @author ctsims
 *
 */
public interface CaseManagementTransitions {
	public void newCase();
	public void followUpOnCase();
	public void viewReferrals();
	public void viewOpen();
	public void closeCase();
	public void exit();
}
