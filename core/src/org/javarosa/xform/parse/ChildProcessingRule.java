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

package org.javarosa.xform.parse;


//unused for now

public class ChildProcessingRule {
	public static final int MULT_NOT_ALLOWED = 1;
	public static final int MULT_PROCESS_FIRST_ONLY = 2;
	public static final int MULT_PROCESS_ALL = 3;
 
	public String name;
	public IElementHandler handler;
	public boolean required;
	public boolean anyLevel;
	public int multiplicity;

	public ChildProcessingRule (String name, IElementHandler handler, boolean required, boolean anyLevel, int multiplicity) {
		this.name = name;
		this.handler = handler;
		this.required = required;
		this.anyLevel = anyLevel;
		this.multiplicity = multiplicity;
	}
}
