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

package org.javarosa.formmanager.view;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

public class FormElementBinding implements FormElementStateListener {
	public IQuestionWidget widget;
	
	public IFormElement element;
	public TreeReference instanceRef;
	public TreeElement instanceNode;
	
	public FormDef form;
	
	private FormElementBinding() {
		
	}

	public FormElementBinding (IQuestionWidget cw, FormIndex index, FormDef form) {
		this(cw, form.getChild(index), index, form);
	}

	public FormElementBinding (IQuestionWidget cw, IFormElement q, FormIndex index, FormDef form) {
		this.widget = cw;
		this.element = q;
		this.instanceRef = form.getChildInstanceRef(index);
        this.instanceNode = form.getDataModel().resolveReference(instanceRef);
        this.form = form;
        
       	register();
	}
	
	//constructor to use with pseudo-questions that aren't actually tied to any form or model
	public FormElementBinding (IQuestionWidget cw, QuestionDef q, TreeElement instanceNode) {
		this.widget = cw;
		this.element = q;
		this.instanceNode = instanceNode;
	}

	public void register () {
		element.registerStateObserver(this);
		instanceNode.registerStateObserver(this);
	}

	public void unregister () {
		element.unregisterStateObserver(this);
		instanceNode.unregisterStateObserver(this);
	}
	
	public IAnswerData getValue () {
		return instanceNode.getValue();
	}
	
	public void formElementStateChanged(IFormElement element, int changeFlags) {
		if (this.element != element)
			throw new IllegalStateException("Widget received event from foreign question");
		if (widget != null)
			widget.refreshWidget(changeFlags);
	}
	
	public void formElementStateChanged(TreeElement instanceNode, int changeFlags) {
		if (this.instanceNode != instanceNode)
			throw new IllegalStateException("Widget received event from foreign question");
		if (widget != null)
			widget.refreshWidget(changeFlags);		
	}
	
	public Object clone() {
		FormElementBinding clone = new FormElementBinding();
		clone.widget = widget;
		clone.element = element;
		clone.instanceNode = instanceNode;
		clone.instanceRef = instanceRef;
		clone.form = form;
		return clone;
	}
}
