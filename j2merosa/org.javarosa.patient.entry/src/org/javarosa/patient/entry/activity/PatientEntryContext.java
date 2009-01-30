/**
 * 
 */
package org.javarosa.patient.entry.activity;

import org.javarosa.core.Context;
import org.javarosa.core.model.utils.IModelProcessor;

/**
 * @author Clayton Sims
 * @date Jan 27, 2009 
 *
 */
public class PatientEntryContext extends Context {
	public static final String TITLE_KEY = "pec_tk";
	public static final String PROCESSOR_KEY = "pec_tk";

	public PatientEntryContext(Context context) {
		super(context);
	}
	
	public void setEntryFormTitle(String title) {
		this.setElement(TITLE_KEY, title);
	}
	
	public String getEntryFormTitle() {
		return (String)this.getElement(TITLE_KEY);
	}
	
	public void setProcessor(IModelProcessor processor) {
		this.setElement(PROCESSOR_KEY, processor);
	}
	
	public IModelProcessor getProcessor() {
		IModelProcessor p = (IModelProcessor)getElement(PROCESSOR_KEY);
		if(p != null) {
			return p;
		} else {
			return new PatientEntryModelProcessor();
		}
	}
}
