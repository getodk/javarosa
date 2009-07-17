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

package org.javarosa.formmanager.utility;

import org.javarosa.core.Context;

/**
 * An interface for specifying actions to occur at the end of a form
 * entry activity. 
 * 
 * @author Clayton Sims
 * @date Jan 30, 2009 
 *
 */
public interface ILoadHost {
	
	/**
	 * A handler method that will be called upon completion of a form entry activity.
	 * @param context The context of the form entry activity that preceded this call.
	 */
	public void returnFromLoading(Context context);
}
