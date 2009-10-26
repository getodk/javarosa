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

package org.javarosa.core.model.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
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
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.storage.WrappingStorageUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapBase;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableWrapper;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class CompactModelWrapper implements WrappingStorageUtility.SerializationWrapper {
	public static final int CHOICE_VALUE = 0;
	public static final int CHOICE_INDEX = 1;
	public static final int CHOICE_MODE = CHOICE_INDEX;
		
	private DataModelTemplateManager templateMgr;
	private DataModelTree model;
	
	public CompactModelWrapper () {
		this(null);
	}
	
	public CompactModelWrapper (DataModelTemplateManager templateMgr) {
		this.templateMgr = templateMgr;
	}
	
	public Class baseType () {
		return DataModelTree.class;
	}
	
	public void setData (Externalizable e) {
		this.model = (DataModelTree)e;
	}
	
	public Externalizable getData () {
		return model;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		int formID = ExtUtil.readInt(in);
		model = getTemplateModel(formID).clone();
		
		model.setID(ExtUtil.readInt(in));
		model.setDateSaved((Date)ExtUtil.read(in, new ExtWrapNullable(Date.class)));
		//formID, name, schema, versions, and namespaces are all invariants of the template model
		
		TreeElement root = model.getRoot();
		readTreeElement(root, initRef(root), in, pf);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		if (model == null) {
			throw new RuntimeException("model has not yet been set via setData()");
		}
		
		ExtUtil.writeNumeric(out, model.getFormId());
		ExtUtil.writeNumeric(out, model.getID());
		ExtUtil.write(out, new ExtWrapNullable(model.getDateSaved()));
				
		TreeElement root = model.getRoot();
		writeTreeElement(out, root, initRef(root));
	}
	
	private DataModelTree getTemplateModel (int formID) {
		if (templateMgr != null) {
			return templateMgr.getTemplateModel(formID);
		} else {
			DataModelTree template = loadTemplateModel(formID);
			if (template == null) {
				throw new RuntimeException("no formdef found for form id [" + formID + "]");
			}
			return template;
		}
	}
	
	public static DataModelTree loadTemplateModel (int formID) {
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		FormDef f = (FormDef)forms.read(formID);
		return (f != null ? f.getDataModel() : null);
	}
	
	private TreeReference initRef (TreeElement root) {
		TreeReference rootRef = TreeReference.rootRef();
		rootRef.add(root.getName(), 0);
		return rootRef;
	}
	
	private TreeReference extendRef (TreeReference ref, String name, int mult) {
		TreeReference childRef = ref.clone();
		childRef.add(name, mult);
		return childRef;
	}

	private void readTreeElement (TreeElement e, TreeReference ref, DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		TreeElement templ = model.getTemplatePath(ref);
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
					
				TreeReference childTemplRef = extendRef(ref, childName, 0);
				TreeElement childTempl = model.getTemplatePath(childTemplRef);
				
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
						TreeReference dstRef = extendRef(ref, childName, j);
						model.copyNode(childTempl, dstRef);
						
						TreeElement child = e.getChild(childName, j);
						child.setRelevant(true);
						readTreeElement(child, dstRef, in, pf);
					}
				} else {
					TreeElement child = e.getChild(childName, 0);
					child.setRelevant(relevant);
					if (relevant) {
						readTreeElement(child, extendRef(ref, childName, 0), in, pf);
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
			case Constants.DATATYPE_GEOPOINT: answerType = GeoPointData.class; break;
			default: answerType = null;
			}
			
			ExternalizableWrapper deserEntity = new ExtWrapNullable(answerType);
			if (answerType == SelectOneData.class) {
				deserEntity = new ExtWrapNullable(CHOICE_MODE == CHOICE_VALUE ? String.class : Integer.class);
			} else if (answerType == SelectMultiData.class) {
				deserEntity = new ExtWrapNullable(new ExtWrapList(CHOICE_MODE == CHOICE_VALUE ? String.class : Integer.class));
			} else if (answerType == null) {
				deserEntity = new ExtWrapNullable(new ExtWrapTagged());
			}

			Object rawVal = ExtUtil.read(in, deserEntity, pf);

			IAnswerData val = null;
			if (rawVal != null) {
				if (answerType == SelectOneData.class) {
					val = getSelectOne(rawVal);
				} else if (answerType == SelectMultiData.class) {
					val = getSelectMulti((Vector)rawVal);
				} else {
					val = (IAnswerData)rawVal;
				}
			}
			
			e.setValue(val);
		}
	}

	private void writeTreeElement (DataOutputStream out, TreeElement e, TreeReference ref) throws IOException {
		TreeElement templ = model.getTemplatePath(ref);
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
						writeTreeElement(out, e.getChild(childName, j), extendRef(ref, childName, j));
					}
				}
			}
		} else {
			IAnswerData val = e.getValue();
			
			Externalizable serEntity = val;
			if (val instanceof SelectOneData) {
				serEntity = compactSelectOne((SelectOneData)val);
			} else if (val instanceof SelectMultiData) {
				serEntity = compactSelectMulti((SelectMultiData)val);
			} else if (e.dataType < 0 || e.dataType >= 100) {
				serEntity = new ExtWrapTagged(val);  //custom data type
			}

			ExtUtil.write(out, new ExtWrapNullable(serEntity));
		}
	}

	private Externalizable compactSelectOne (SelectOneData data) {
		Selection val = (Selection)data.getValue();
		return new ExtWrapBase(extractSelection(val));
	}
	
	private Externalizable compactSelectMulti (SelectMultiData data) {
		Vector val = (Vector)data.getValue();
		Vector choices = new Vector();
		for (int i = 0; i < val.size(); i++) {
			choices.addElement(extractSelection((Selection)val.elementAt(i)));
		}
		return new ExtWrapList(choices);
	}

	private SelectOneData getSelectOne (Object o) {
		return new SelectOneData(makeSelection(o));
	}
	
	private SelectMultiData getSelectMulti (Vector v) {
		Vector choices = new Vector();
		for (int i = 0; i < v.size(); i++) {
			choices.addElement(makeSelection(v.elementAt(i)));
		}
		return new SelectMultiData(choices);
	}
			
	private Object extractSelection (Selection s) {
		switch (CHOICE_MODE) {
		case CHOICE_VALUE:
			return s.getValue();
		case CHOICE_INDEX:
			if (s.index == -1) {
				throw new RuntimeException("trying to serialize in choice-index mode but selections do not have indexes set!");
			}
			return new Integer(s.index);
		default: throw new IllegalArgumentException();
		}
	}
	
	private Selection makeSelection (Object o) {
		Selection s = new Selection();
		if (o instanceof String) {
			s.xmlValue = (String)o;
		} else if (o instanceof Integer) {
			s.index = ((Integer)o).intValue();
		} else {
			throw new RuntimeException();
		}
		return s;
	}
	
}
