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

package org.javarosa.formmanager.view.clforms.acquire;

import org.javarosa.formmanager.view.FormElementBinding;

/**
 * @author mel
 * 
 * A service that provides data acquisition widgets
 *
 */
public interface IAcquiringService {
	
	public AcquiringQuestionScreen getWidget(FormElementBinding prompt, int temp);

	public AcquiringQuestionScreen getWidget(FormElementBinding prompt);

	public AcquiringQuestionScreen getWidget(FormElementBinding prompt,
			String str);

	public AcquiringQuestionScreen getWidget(FormElementBinding prompt, char str);
}
