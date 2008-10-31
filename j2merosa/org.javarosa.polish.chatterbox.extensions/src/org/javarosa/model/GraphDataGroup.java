package org.javarosa.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.patient.model.data.NumericListData;
import org.javarosa.patient.util.DateValueTuple;

public class GraphDataGroup extends QuestionDataGroup {
	public static final int GRAPH_DATA_ID = 11;
	
	IDataReference reference;
	
	public GraphDataGroup() {
		super();
	}
	
	public void setReference(IDataReference reference) {
		this.reference = reference;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.instance.TreeElement#matchesReference(org.javarosa.core.model.IDataReference)
	 */
	public boolean matchesReference(IDataReference reference) {
		return reference.referenceMatches(this.reference); 
	}
	
	public IAnswerData getValue() {
		NumericListData returnVal = new NumericListData();
		Vector children = this.getChildren();
		Enumeration en = children.elements();
		while(en.hasMoreElements()) {
			TreeElement element = (TreeElement)en.nextElement();
			if(element instanceof QuestionDataGroup) {
				QuestionDataGroup group = ((QuestionDataGroup)element);
				Vector subchildren = group.getChildren();
				
				Date dateValue = null;
				Integer intValue = null;
				Enumeration subEn = subchildren.elements();
				while(subEn.hasMoreElements()) {
					TreeElement subElement = (TreeElement)subEn.nextElement();
					if("date".equals(subElement.getName())) {
						IAnswerData dateAnswer = (IAnswerData)subElement.getValue();
						if(dateAnswer != null) {
							dateValue = (Date)dateAnswer.getValue();
						}
					}
					if("value".equals(subElement.getName())) {
						IAnswerData valueAnswer = (IAnswerData)subElement.getValue();
						if(valueAnswer != null) {
							Object answer = valueAnswer.getValue();
							if(answer instanceof String) {
								intValue = Integer.valueOf(((String)answer));
							} else if(answer instanceof Integer) {
								intValue = (Integer)answer;
							}
						}
					}
				}
				if(intValue != null) {
					if(dateValue == null) {
						dateValue = new Date();
					}
					returnVal.addMeasurement(new DateValueTuple(dateValue, intValue.intValue()));
				}
				
			}
		}
		return returnVal;
	}
	
	public void setValue(IAnswerData data) {
		if(data instanceof NumericListData) {
			clearDataNodes();
			Vector values = (Vector)((NumericListData) data).getValue();
			Enumeration en  = values.elements();
			while(en.hasMoreElements()) {
				DateValueTuple element = ((DateValueTuple)en.nextElement());
				QuestionDataGroup dataNode = new QuestionDataGroup("data");
				//The this.reference thing? Sketchy
				QuestionDataElement date = new QuestionDataElement("date", this.reference, new DateData(element.date));
				QuestionDataElement value = new QuestionDataElement("value", this.reference, new IntegerData(element.value));
				dataNode.addChild(date);
				dataNode.addChild(value);
				this.addChild(dataNode);
			}
		}
	}
	
	private void clearDataNodes() {
		Vector children = this.getChildren();
		Enumeration en = children.elements();
		while(en.hasMoreElements()) {
			TreeElement element = (TreeElement)en.nextElement();
			
			if(element.getName() == "data") {
				this.removeChild(element);
			}
		}
	}
	
	
	public void readNodeAttributes(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readNodeAttributes(in, pf);
		reference = (IDataReference)ExtUtil.read(in, new ExtWrapTagged(), pf);
	}
	public void writeNodeAttributes(DataOutputStream out) throws IOException {
		super.writeNodeAttributes(out);
		ExtUtil.write(out, new ExtWrapTagged(reference));
	}
	
}
