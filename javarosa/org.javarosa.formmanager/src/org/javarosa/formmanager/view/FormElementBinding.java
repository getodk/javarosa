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

	public FormElementBinding (IQuestionWidget cw, FormIndex index, FormDef form) {
		this(cw, form.getChild(index), index, form);
	}

	public FormElementBinding (IQuestionWidget cw, IFormElement q, FormIndex index, FormDef form) {
		this.widget = cw;
		this.element = q;
		this.instanceRef = form.getChildInstanceRef(index);
        this.instanceNode = form.getDataModel().resolveReference(instanceRef);
		
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
}
