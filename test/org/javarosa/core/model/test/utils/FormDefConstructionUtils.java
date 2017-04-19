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

package org.javarosa.core.model.test.utils;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.QuestionDef;

/**
 * Static methods to create form defs for workflow testing.
 * 
 * @author Clayton Sims
 *
 */
public class FormDefConstructionUtils {
	
	public static FormDef createSimpleGroupReference() {
		FormDef theform = new FormDef();
		
		QuestionDef question1 = new QuestionDef();
		GroupDef group1 = new GroupDef();
		QuestionDef question11 = new QuestionDef();
		QuestionDef question12 = new QuestionDef();
		group1.addChild(question11);
		group1.addChild(question12);
		QuestionDef question2 = new QuestionDef();
		theform.addChild(question1);
		theform.addChild(group1);
		theform.addChild(question2);
		
		return theform;
	}
}
