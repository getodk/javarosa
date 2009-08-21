/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
	
	public IModelProcessor getModelProcessor(String formType, int thisidisahack);
	
	public IEntity getUniqueEntity();
}
