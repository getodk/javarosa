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
package org.javarosa.core.model.utils;

import org.javarosa.core.Context;
import org.javarosa.core.model.instance.DataModelTree;

/**
 * An interface for classes which are capable of parsing and performing actions
 * on Data Model objects.
 * 
 * @author Clayton Sims
 * @date Jan 27, 2009 
 *
 */
public interface IModelProcessor {
	
	/**
	 * Processes the provided data model.
	 * 
	 * @param tree The data model that will be handled.
	 */
	public void processModel(DataModelTree tree);
	
	/**
	 * Initializes this model processor with the provided
	 * context.
	 * 
	 * @param context An activity context
	 */
	public void initializeContext(Context context);
	
	/**
	 * Loads the provided context model with any information
	 * obtained from processing the models provided to this
	 * model processor.
	 * 
	 * @param context An activity context
	 */
	public void loadProcessedContext(Context context);
}
