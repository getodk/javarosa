/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.core.model.utils.IModelProcessor;
import org.javarosa.entity.model.IEntity;

/**
 * @author Clayton Sims
 * @date Mar 20, 2009 
 *
 */
public interface ICaseType {
	
	public final static String FORM_TYPE_NEW_CASE = "c_ft_nc";
	public final static String FORM_TYPE_FOLLOWUP = "c_ft_fu";
	public final static String FORM_TYPE_CLOSE = "c_ft_cc";
	public final static String FORM_TYPE_REFERRAL = "c_ft_ref";
	
	public String getCaseTypeId();
	
	public String getCaseTypeName();
	
	public String getFormName(String formType);
	
	public IModelProcessor getModelProcessor(String formType);
	
	public IEntity getUniqueEntity();
}
