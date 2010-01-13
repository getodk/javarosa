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

package org.javarosa.core.model.instance.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
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

/**
 * An alternate serialization format for DataModelTrees (saved form instances) that drastically reduces the 
 * resultant record size by cutting out redundant information. Size savings are typically 90-95%. The trade-off is
 * that in order to deserialize, a template DataModelTree (typically from the original FormDef) must be provided.
 * 
 * In general, the format is thus:
 * 1) write the fields from the DataModelTree object (e.g., date saved), excluding those that never change for a given
 *    form type (e.g., schema).
 * 2) walk the tree depth-first. for each node: if repeatable, write the number of repetitions at the current level; if
 *    not, write a boolean indicating if the node is relevant. non-relevant nodes are not descended into. repeated nodes
 *    (i.e., several nodes with the same name at the current level) are handled in order
 * 3) for each leaf (data) node, write a boolean whether the node is empty or has data
 * 4) if the node has data, serialize the data. do not specify the data type -- it can be determined from the template.
 *    multiple choice questions use a more compact format than normal.
 * 4a) in certain situations where the data differs from its prescribed data type (can happen as the result of 'calculate'
 *    expressions), flag the actual data type by hijacking the 'empty' flag above
 * 
 * @author Drew Roos
 *
 */
public class CompactModelWrapper implements WrappingStorageUtility.SerializationWrapper {
	public static final int CHOICE_VALUE = 0;	/* serialize multiple-select choices by writing out the <value> */
	public static final int CHOICE_INDEX = 1;   /* serialize multiple-select choices by writing out only the index of the
	                                             * choice; much more compact than CHOICE_VALUE, but the deserialized
	                                             * model must be explicitly re-attached to the parent FormDef (not just
	                                             * the template data model) before the instance can be serialized to xml
	                                             * (otherwise the actual xml <value>s are still unknown)
	                                             */

	public static final int CHOICE_MODE = CHOICE_INDEX;
	
	private DataModelTemplateManager templateMgr;	/* model template provider; provides templates needed for deserialization. */
	private DataModelTree model;					/* underlying DataModelTree to serialize/deserialize */
	
	public CompactModelWrapper () {
		this(null);
	}
	
	/**
	 * 
	 * @param templateMgr template provider; if null, template is always fetched on-demand from RMS (slow!)
	 */
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

	/**
	 * deserialize a compact model. note the retrieval of the template data model
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		int formID = ExtUtil.readInt(in);
		model = getTemplateModel(formID).clone();
		
		model.setID(ExtUtil.readInt(in));
		model.setDateSaved((Date)ExtUtil.read(in, new ExtWrapNullable(Date.class)));
		//formID, name, schema, versions, and namespaces are all invariants of the template model
		
		TreeElement root = model.getRoot();
		readTreeElement(root, initRef(root), in, pf);
	}
	
	/**
	 * serialize a compact model
	 */
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
	
	/**
	 * load a template model fresh from the original FormDef, retrieved from RMS
	 * @param formID
	 * @return
	 */
	public static DataModelTree loadTemplateModel (int formID) {
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		FormDef f = (FormDef)forms.read(formID);
		return (f != null ? f.getDataModel() : null);
	}
	
	/**
	 * create the tree reference for the data model root node
	 * @param root
	 * @return
	 */
	private static TreeReference initRef (TreeElement root) {
		TreeReference rootRef = TreeReference.rootRef();
		rootRef.add(root.getName(), 0);
		return rootRef;
	}
	
	/**
	 * clone and extend a reference by one level
	 * @param ref
	 * @param name
	 * @param mult
	 * @return
	 */
	private static TreeReference extendRef (TreeReference ref, String name, int mult) {
		TreeReference childRef = ref.clone();
		childRef.add(name, mult);
		return childRef;
	}

	/**
	 * recursively read in a node of the instance, by filling out the template model
	 * @param e
	 * @param ref
	 * @param in
	 * @param pf
	 * @throws IOException
	 * @throws DeserializationException
	 */
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
			e.setValue((IAnswerData)ExtUtil.read(in, new ExtWrapAnswerData(e.dataType)));
		}
	}

	/**
	 * recursively write out a node of the instance
	 * @param out
	 * @param e
	 * @param ref
	 * @throws IOException
	 */
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
			ExtUtil.write(out, new ExtWrapAnswerData(e.dataType, e.getValue()));
		}
	}

	/**
	 * ExternalizableWrapper to handle writing out a node's data. In particular, handles:
	 *   * empty nodes
	 *   * ultra-compact serialization of multiple-choice answers
	 *   * tagging with extra type information when the template alone will not contain sufficient information
	 * 
	 * @author Drew Roos
	 *
	 */
	private class ExtWrapAnswerData extends ExternalizableWrapper {
		int dataType;
		
		public ExtWrapAnswerData (int dataType, IAnswerData val) {
			this.val = val;
			this.dataType = dataType;
		}
		
		public ExtWrapAnswerData (int dataType) {
			this.dataType = dataType;
		}
		
		public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
			byte flag = in.readByte();
			if (flag == 0x00) {
				val = null;
			} else {
				Class answerType = classForDataType(dataType);

				if (answerType == null) {
					//custom data types
					val = ExtUtil.read(in, new ExtWrapTagged(), pf);
				} else if (answerType == SelectOneData.class) {
					val = getSelectOne(ExtUtil.read(in, CHOICE_MODE == CHOICE_VALUE ? String.class : Integer.class));
				} else if (answerType == SelectMultiData.class) {
					val = getSelectMulti((Vector)ExtUtil.read(in, new ExtWrapList(CHOICE_MODE == CHOICE_VALUE ? String.class : Integer.class)));
				} else {
					switch (flag) {
					case 0x40: answerType = StringData.class; break;
					case 0x41: answerType = IntegerData.class; break;
					case 0x42: answerType = DecimalData.class; break;
					case 0x43: answerType = DateData.class; break;
					}
					
					val = (IAnswerData)ExtUtil.read(in, answerType);
				}					
			}
		}

		public void writeExternal(DataOutputStream out) throws IOException {
			if (val == null) {
				out.writeByte(0x00);
			} else {
				byte prefix = 0x01;
				Externalizable serEntity;
				
				if (dataType < 0 || dataType >= 100) {
					//custom data types
					serEntity = new ExtWrapTagged(val);
				} else if (val instanceof SelectOneData) {
					serEntity = new ExtWrapBase(compactSelectOne((SelectOneData)val));
				} else if (val instanceof SelectMultiData) {
					serEntity = new ExtWrapList(compactSelectMulti((SelectMultiData)val));
				} else {
					serEntity = (IAnswerData)val;
					
					//flag when data type differs from the default data type in the <bind> (can happen with 'calculate's)
					if (val.getClass() != classForDataType(dataType)) {
						if (val instanceof StringData) {
							prefix = 0x40;
						} else if (val instanceof IntegerData) {
							prefix = 0x41;
						} else if (val instanceof DecimalData) {
							prefix = 0x42;
						} else if (val instanceof DateData) {
							prefix = 0x43;
						} else {
							throw new RuntimeException("divergent data type not allowed");
						}
					}
				}

				out.writeByte(prefix);
				ExtUtil.write(out, serEntity);
			}
		}

		public ExternalizableWrapper clone(Object val) {
			throw new RuntimeException("not supported");				
		}

		public void metaReadExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
			throw new RuntimeException("not supported");	
		}

		public void metaWriteExternal(DataOutputStream out) throws IOException {
			throw new RuntimeException("not supported");			
		}		
	}

	/**
	 * reduce a SelectOneData to an integer (index mode) or string (value mode)
	 * @param data
	 * @return Integer or String
	 */
	private Object compactSelectOne (SelectOneData data) {
		Selection val = (Selection)data.getValue();
		return extractSelection(val);
	}
	
	/**
	 * reduce a SelectMultiData to a vector of integers (index mode) or strings (value mode)
	 * @param data
	 * @return
	 */
	private Vector compactSelectMulti (SelectMultiData data) {
		Vector val = (Vector)data.getValue();
		Vector choices = new Vector();
		for (int i = 0; i < val.size(); i++) {
			choices.addElement(extractSelection((Selection)val.elementAt(i)));
		}
		return choices;
	}

	/**
	 * create a SelectOneData from an integer (index mode) or string (value mode)
	 */
	private SelectOneData getSelectOne (Object o) {
		return new SelectOneData(makeSelection(o));
	}
	
	/**
	 * create a SelectMultiData from a vector of integers (index mode) or strings (value mode)
	 */
	private SelectMultiData getSelectMulti (Vector v) {
		Vector choices = new Vector();
		for (int i = 0; i < v.size(); i++) {
			choices.addElement(makeSelection(v.elementAt(i)));
		}
		return new SelectMultiData(choices);
	}
	
	/**
	 * extract the value out of a Selection according to the current CHOICE_MODE
	 * @param s
	 * @return Integer or String
	 */
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
	
	/**
	 * build a Selection from an integer or string, according to the current CHOICE_MODE
	 * @param o
	 * @return
	 */
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
	
	/**
	 * map xforms data types to the Class that represents that data in a DataModelTree
	 * @param dataType
	 * @return
	 */
	private Class classForDataType (int dataType) {
		switch (dataType) {
		case Constants.DATATYPE_NULL: return StringData.class;
		case Constants.DATATYPE_TEXT: return StringData.class;
		case Constants.DATATYPE_INTEGER: return IntegerData.class;
		case Constants.DATATYPE_DECIMAL: return DecimalData.class;
		case Constants.DATATYPE_DATE: return DateData.class;
		case Constants.DATATYPE_TIME: return TimeData.class;
		case Constants.DATATYPE_DATE_TIME: return DateTimeData.class;
		case Constants.DATATYPE_CHOICE: return SelectOneData.class;
		case Constants.DATATYPE_CHOICE_LIST: return SelectMultiData.class;
		case Constants.DATATYPE_GEOPOINT: return GeoPointData.class;
		default: return null;
		}
	}
	
	//random utility code; putting it here for now
	
	/**
	 * Link a deserialized instance back up with its parent FormDef. this allows select/select1 questions to be
	 * internationalizable in chatterbox, and (if using CHOICE_INDEX mode) allows the instance to be serialized
	 * to xml
	 */
	public static void linkSelectQuestions (DataModelTree model, FormDef f) {
		if (f.getID() != model.getFormId()) {
			throw new RuntimeException("model is not compatible with formdef");
		}
		
		TreeElement root = model.getRoot();
		linkSelectQuestions(root, initRef(root), f);
	}
		
	private static void linkSelectQuestions (TreeElement node, TreeReference ref, FormDef f) {
		for (int i = 0; i < node.getNumChildren(); i++) {
			TreeElement child = (TreeElement)node.getChildren().elementAt(i);
			linkSelectQuestions(child, extendRef(ref, child.getName(), TreeReference.INDEX_UNBOUND), f);
		}
		
		IAnswerData val = node.getValue();
		Vector selections = null;
		if (val instanceof SelectOneData) {
			selections = new Vector();
			selections.addElement(val.getValue());
		} else if (val instanceof SelectMultiData) {
			selections = (Vector)val.getValue();
		}
			
		if (selections != null) {
			QuestionDef q = findQuestionByRef(ref, f);
			if (q == null) {
				throw new RuntimeException("can't find question to link");
			}
			
			for (int i = 0; i < selections.size(); i++) {
				Selection s = (Selection)selections.elementAt(i);
				s.attachQuestionDef(q);
			}
		}
	}
		
	private static QuestionDef findQuestionByRef (TreeReference ref, IFormElement fe) {
		if (fe instanceof QuestionDef) {
			QuestionDef q = (QuestionDef)fe;
			TreeReference bind = (TreeReference)q.getBind().getReference();
			return (ref.equals(bind) ? q : null);
		} else {
			for (int i = 0; i < fe.getChildren().size(); i++) {
				QuestionDef ret = findQuestionByRef(ref, fe.getChild(i));
				if (ret != null)
					return ret;
			}
			return null;
		}
	}
}
