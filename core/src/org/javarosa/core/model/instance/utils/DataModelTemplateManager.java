package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.FormInstance;

/**
 * Used by CompactModelWrapper to retrieve the template DataModelTrees (from the original FormDef)
 * necessary to unambiguously deserialize the compact models
 * 
 * @author Drew Roos
 *
 */
public interface DataModelTemplateManager {

	/**
	 * return FormInstance for the FormDef with the given form ID
	 * @param formID
	 * @return
	 */
	FormInstance getTemplateModel (int formID);

}

