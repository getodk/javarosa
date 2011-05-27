package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.FormInstance;

/**
 * Used by CompactInstanceWrapper to retrieve the template FormInstances (from the original FormDef)
 * necessary to unambiguously deserialize the compact models
 * 
 * @author Drew Roos
 *
 */
public interface InstanceTemplateManager {

	/**
	 * return FormInstance for the FormDef with the given form ID
	 * @param formID
	 * @return
	 */
	FormInstance getTemplateInstance (int formID);

}

