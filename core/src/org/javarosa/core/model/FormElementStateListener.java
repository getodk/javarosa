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

package org.javarosa.core.model;

import org.javarosa.core.model.instance.TreeElement;

/**
 * @author Drew Roos?
 *
 */
public interface FormElementStateListener {
	static final int CHANGE_INIT = 0x00;
	static final int CHANGE_DATA = 0x01;
	static final int CHANGE_LOCALE = 0x02;
	static final int CHANGE_ENABLED = 0x04;
	static final int CHANGE_RELEVANT = 0x08;
	static final int CHANGE_REQUIRED = 0x10;
//	static final int CHANGE_LOCKED = 0x20;
	static final int CHANGE_OTHER = 0x40;

	void formElementStateChanged (IFormElement question, int changeFlags);
	
	void formElementStateChanged (TreeElement question, int changeFlags);
}