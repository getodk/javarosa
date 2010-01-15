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

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.chatterbox.extendedwidget.GraphWidget;
import org.kxml2.kdom.Element;

public class GraphElementHandler implements IElementHandler{
	private Vector graphTypes = new Vector();
	
	public void registerGraphType(String type) {
		graphTypes.addElement(type);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.xform.parse.IElementHandler#handle(org.javarosa.core.model.FormDef, org.kxml2.kdom.Element, java.lang.Object)
	 */
	public void handle(FormDef f, Element e, Object parent) {
		parseControl((IFormElement)parent, e, f, GraphWidget.CONTROL_GRAPH);
		
	}

	private void parseControl (IFormElement parent, Element e, FormDef f, int controlType) {
		QuestionDef question = XFormParser.parseControl(parent, e, f, controlType);
		String type = e.getAttributeValue(null,"type");
		if (type != null) {
			if (graphTypes.contains(type)) {
				question.setAppearanceAttr(type);
				return;
			}
		}
		//We didn't set the datatype. don't want to get one on accident.
		question.setAppearanceAttr(null);
	}
}
