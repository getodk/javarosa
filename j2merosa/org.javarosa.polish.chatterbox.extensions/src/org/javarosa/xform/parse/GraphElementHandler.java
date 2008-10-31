package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.util.Map;
import org.javarosa.formmanager.view.chatterbox.widget.GraphWidget;
import org.kxml2.kdom.Element;

public class GraphElementHandler implements IElementHandler{
	private Map graphTypes = new Map();
	
	public void registerGraphType(String type, int typeVal) {
		graphTypes.put(type, new Integer(typeVal));
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
			Integer typeVal = (Integer) graphTypes.get(type);
			if (typeVal != null) {
				question.setDataType(typeVal.intValue());
				return;
			}
		}
		//We didn't set the datatype. don't want to get one on accident.
		question.setDataType(-1);
	}
}
