package org.javarosa.patient.entry.model;

/**
 * NOTICE:
 * 
 * 
 * This class, and the entire org.javarosa.patient.entry Module all only
 * exist because <group> and <repeat> are not yet available in our XForms 
 * subset. 
 * 
 * THIS MODULE IS FULL OF CODE THAT SHOULD NOT BE EMULATED.
 * 
 * Please do not take the code in the org.javarosa.patient.entry project
 * as an example of a proper use of the JavaRosa code base. It will be removed
 * as soon as it is possible to represent the logic in the PatientEntryForm in
 * our XForms subset.
 * 
 * @author Clayton Sims
 *
 */
public interface ITriggerHandler {
	public void handle();
}
