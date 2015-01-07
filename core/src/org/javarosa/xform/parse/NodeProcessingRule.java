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

import java.util.HashMap;

//unused for now

public class NodeProcessingRule {
	public String name;
	public boolean allowUnknownChildren;
	public boolean allowChildText;
	public HashMap<String,ChildProcessingRule> childRules;
	
	public NodeProcessingRule (String name, boolean allowUnknownChildren, boolean allowChildText) {
		this.name = name;
		this.allowUnknownChildren = allowUnknownChildren;
		this.allowChildText = allowChildText;
		childRules = new HashMap<String,ChildProcessingRule>();
	}
	
	public void addChild (ChildProcessingRule cpr) {
		childRules.put(cpr.name, cpr);
	}
}
