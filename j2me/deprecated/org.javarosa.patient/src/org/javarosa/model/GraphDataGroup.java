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

package org.javarosa.model;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.patient.model.data.NumericListData;
import org.javarosa.patient.util.DateValueTuple;

public class GraphDataGroup extends TreeElement {
	public static final int GRAPH_DATA_ID = 11;
	
	IDataReference reference;
	
	public GraphDataGroup() {
		super();
	}
	
	public void setReference(IDataReference reference) {
		this.reference = reference;
	}
	
	
	public IAnswerData getValue() {
		NumericListData returnVal = new NumericListData();
		for (int k1 = 0; k1 < this.getNumChildren(); k1++) {
			TreeElement element = this.getChildAt(k1);
				
				Date dateValue = null;
				Integer intValue = null;
				
				for (int k2 = 0; k2 < element.getNumChildren(); k2++) {
					TreeElement subElement = element.getChildAt(k2);
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
		return returnVal;
	}
	
	public void setValue(IAnswerData data) {
		if(data instanceof NumericListData) {
			clearDataNodes();
			Vector values = (Vector)((NumericListData) data).getValue();
			Enumeration en  = values.elements();
			while(en.hasMoreElements()) {
				DateValueTuple element = ((DateValueTuple)en.nextElement());
				TreeElement dataNode = new TreeElement("data");
				//The this.reference thing? Sketchy
				TreeElement date = new TreeElement("date");
				date.setValue(new DateData(element.date));
				TreeElement value = new TreeElement("value");
				value.setValue(new IntegerData(element.value));
				dataNode.addChild(date);
				dataNode.addChild(value);
				this.addChild(dataNode);
			}
		}
	}
	
	private void clearDataNodes() {
		for (int i = 0; i < this.getNumChildren(); i++) {
			TreeElement element = this.getChildAt(i);
			
			if(element.getName() == "data") {
				this.removeChild(element);
			}
		}
	}
}
