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

package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class BareBonesDataModelWrapper implements Externalizable {
	public static final boolean GET_SELECT_VALUES_FROM_FORMDEF = true;
	
	private DataModelTree dm;
	
	public BareBonesDataModelWrapper (DataModelTree dm) {
		this.dm = dm;
	}
	
	public DataModelTree getDataModel () {
		return dm;
	}
	
	public static DataModelTree getDataModel (DataInputStream in, DataModelTree template) throws IOException, DeserializationException {
		BareBonesDataModelWrapper bbdmw = new BareBonesDataModelWrapper(template);
		bbdmw.readExternal(in, ExtUtil.defaultPrototypes());
		return bbdmw.getDataModel();
	}

	private static DataModelTree cloneDataModel (DataModelTree orig) {
		DataModelTree copy = new DataModelTree(orig.getRoot().deepCopy(true));
		cloneDataModelFrom(copy, orig);
		return copy;
	}
		
	private static void cloneDataModelFrom (DataModelTree dst, DataModelTree src) {
		dst.setID(src.getID());
		dst.setFormId(src.getFormId());
		dst.setName(src.getName());
		dst.setDateSaved(src.getDateSaved());
		dst.schema = src.schema;
		//dst.setRoot(src.getRoot().deepCopy(true));
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		if (dm == null) {
			throw new DeserializationException("Template data model is not set!");
		}
		
		dm.setID(ExtUtil.readInt(in));
		dm.setFormId(ExtUtil.readInt(in));
		dm.setName(ExtUtil.nullIfEmpty(ExtUtil.readString(in)));
		dm.schema = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		dm.setDateSaved((Date)ExtUtil.read(in, new ExtWrapNullable(Date.class)));
		
		TreeElement root = dm.getRoot();
		TreeReference rootRef = TreeReference.rootRef();
		rootRef.add(root.getName(), 0);
		readTreeElement(root, rootRef, in, pf);
	}

	private void readTreeElement (TreeElement e, TreeReference ref, DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		TreeElement templ = dm.getTemplatePath(ref);
		boolean isGroup = !templ.isLeaf();
				
		if (isGroup) {
			Vector childTypes = new Vector();
			for (int i = 0; i < templ.getNumChildren(); i++) {
				String childName = ((TreeElement)templ.getChildren().elementAt(i)).getName();				
				if (!childTypes.contains(childName)) {
					childTypes.addElement(childName);
				}
			}
			
			for (int i = 0; i < childTypes.size(); i++) {
				String childName = (String)childTypes.elementAt(i);
					
				TreeReference childTemplRef = ref.clone();
				childTemplRef.add(childName, 0);
				TreeElement childTempl = dm.getTemplatePath(childTemplRef);
				
				boolean repeatable = childTempl.repeatable;
				int n = ExtUtil.readInt(in);
				boolean relevant = (n > 0);
				if (!repeatable && n > 1) {
					throw new DeserializationException("Detected repeated instances of a non-repeatable node");
				}
			
				if (repeatable) {
					int mult = e.getChildMultiplicity(childName);
					for (int j = mult - 1; j >= 0; j--) {
						e.removeChild(childName, j);
					}
					
					for (int j = 0; j < n; j++) {
						TreeReference dstRef = ref.clone();
						dstRef.add(childName, j);
						dm.copyNode(childTempl, dstRef);
						
						TreeElement child = e.getChild(childName, j);
						child.setRelevant(true);
						TreeReference childRef = ref.clone();
						childRef.add(childName, j);
						readTreeElement(child, childRef, in, pf);
					}
				} else {
					TreeElement child = e.getChild(childName, 0);
					child.setRelevant(relevant);
					if (relevant) {
						TreeReference childRef = ref.clone();
						childRef.add(childName, 0);
						readTreeElement(child, childRef, in, pf);
					}
				}
			}
		} else {
			Class answerType;
			switch (e.dataType) {
			case Constants.DATATYPE_NULL: answerType = StringData.class; break;
			case Constants.DATATYPE_TEXT: answerType = StringData.class; break;
			case Constants.DATATYPE_INTEGER: answerType = IntegerData.class; break;
			case Constants.DATATYPE_DECIMAL: answerType = DecimalData.class; break;
			case Constants.DATATYPE_DATE: answerType = DateData.class; break;
			case Constants.DATATYPE_TIME: answerType = TimeData.class; break;
			case Constants.DATATYPE_DATE_TIME: answerType = DateTimeData.class; break;
			case Constants.DATATYPE_CHOICE: answerType = SelectOneData.class; break;
			case Constants.DATATYPE_CHOICE_LIST: answerType = SelectMultiData.class; break;
		//	case Constants.DATATYPE_BOOLEAN: answerType = *Data.class; break;
			case Constants.DATATYPE_GEOPOINT: answerType = GeoPointData.class; break;
			default: answerType = null;
			}
			
			IAnswerData val;
			if (answerType != null) {
				val = (IAnswerData)ExtUtil.read(in, new ExtWrapNullable(answerType));
			} else {
				val = (IAnswerData)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
			}
			e.setValue(val);
		}
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		if (GET_SELECT_VALUES_FROM_FORMDEF) {
			dm = cloneDataModel(dm);
			purgeSelectValues(dm.getRoot());
		}
		
		ExtUtil.writeNumeric(out, dm.getID());
		ExtUtil.writeNumeric(out, dm.getFormId());
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(dm.getName()));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(dm.schema));
		ExtUtil.write(out, new ExtWrapNullable(dm.getDateSaved()));
		
		TreeElement root = dm.getRoot();
		TreeReference rootRef = TreeReference.rootRef();
		rootRef.add(root.getName(), 0);
		writeTreeElement(out, root, rootRef);
	}

	private void writeTreeElement (DataOutputStream out, TreeElement e, TreeReference ref) throws IOException {
		TreeElement templ = dm.getTemplatePath(ref);
		boolean isGroup = !templ.isLeaf();
				
		if (isGroup) {
			Vector childTypesHandled = new Vector();
			for (int i = 0; i < templ.getNumChildren(); i++) {
				String childName = ((TreeElement)templ.getChildren().elementAt(i)).getName();
				if (!childTypesHandled.contains(childName)) {
					childTypesHandled.addElement(childName);
					
					int mult = e.getChildMultiplicity(childName);
					if (mult > 0 && !e.getChild(childName, 0).isRelevant()) {
						mult = 0;
					}
					
					ExtUtil.writeNumeric(out, mult);
					for (int j = 0; j < mult; j++) {
						TreeElement child = e.getChild(childName, j);
						TreeReference childRef = ref.clone();
						childRef.add(childName, j);
						writeTreeElement(out, child, childRef);
					}
				}
			}
		} else {
			IAnswerData val = e.getValue();
			if (e.dataType >= 0 && e.dataType < 100) { //100 is upper range of reserved JavaRosa datatypes
				ExtUtil.write(out, new ExtWrapNullable(val));				
			} else {
				ExtUtil.write(out, new ExtWrapNullable(new ExtWrapTagged(val)));
			}
		}
	}
	
	private void purgeSelectValues (TreeElement e) {
		if (e.isChildable()) {
			for (int i = 0; i < e.getNumChildren(); i++) {
				purgeSelectValues((TreeElement)e.getChildren().elementAt(i));
			}
		} else {
			IAnswerData val = e.getValue();
			if (val instanceof SelectOneData || val instanceof SelectMultiData) {
				Vector selections;
				if (val instanceof SelectMultiData) {
					selections = (Vector)val.getValue();
				} else {
					selections = new Vector();
					selections.addElement((Selection)val.getValue());
				}
				
				for (int i = 0; i < selections.size(); i++) {
					Selection s = (Selection)selections.elementAt(i);
					s.question = null;
					s.xmlValue = "";
				}
			}
		}
	}
	
}
