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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.GeoTraceData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InvalidReferenceException;
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
 * An alternate serialization format for FormInstances (saved form instances) that drastically reduces the
 * resultant record size by cutting out redundant information. Size savings are typically 90-95%. The trade-off is
 * that in order to deserialize, a template FormInstance (typically from the original FormDef) must be provided.
 *
 * In general, the format is thus:
 * 1) write the fields from the FormInstance object (e.g., date saved), excluding those that never change for a given
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
public class CompactInstanceWrapper implements WrappingStorageUtility.SerializationWrapper {
	public static final int CHOICE_VALUE = 0;	/* serialize multiple-select choices by writing out the <value> */
	public static final int CHOICE_INDEX = 1;   /* serialize multiple-select choices by writing out only the index of the
	                                             * choice; much more compact than CHOICE_VALUE, but the deserialized
	                                             * instance must be explicitly re-attached to the parent FormDef (not just
	                                             * the template data instance) before the instance can be serialized to xml
	                                             * (otherwise the actual xml <value>s are still unknown)
	                                             */

	public static final int CHOICE_MODE = CHOICE_INDEX;

	private InstanceTemplateManager templateMgr;	/* instance template provider; provides templates needed for deserialization. */
	private FormInstance instance;					/* underlying FormInstance to serialize/deserialize */

	public CompactInstanceWrapper () {
		this(null);
	}

	/**
	 *
	 * @param templateMgr template provider; if null, template is always fetched on-demand from RMS (slow!)
	 */
	public CompactInstanceWrapper (InstanceTemplateManager templateMgr) {
		this.templateMgr = templateMgr;
	}

	public Class baseType () {
		return FormInstance.class;
	}

	public void setData (Externalizable e) {
		this.instance = (FormInstance)e;
	}

	public Externalizable getData () {
		return instance;
	}

	/**
	 * deserialize a compact instance. note the retrieval of the template data instance
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		int formID = ExtUtil.readInt(in);
		instance = getTemplateInstance(formID).clone();

		instance.setID(ExtUtil.readInt(in));
		instance.setDateSaved((Date)ExtUtil.read(in, new ExtWrapNullable(Date.class)));
		//formID, name, schema, versions, and namespaces are all invariants of the template instance

		TreeElement root = instance.getRoot();
		readTreeElement(root, in, pf);
	}

	/**
	 * serialize a compact instance
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		if (instance == null) {
			throw new RuntimeException("instance has not yet been set via setData()");
		}

		ExtUtil.writeNumeric(out, instance.getFormId());
		ExtUtil.writeNumeric(out, instance.getID());
		ExtUtil.write(out, new ExtWrapNullable(instance.getDateSaved()));

		writeTreeElement(out, instance.getRoot());
	}

	private FormInstance getTemplateInstance (int formID) {
		if (templateMgr != null) {
			return templateMgr.getTemplateInstance(formID);
		} else {
			FormInstance template = loadTemplateInstance(formID);
			if (template == null) {
				throw new RuntimeException("no formdef found for form id [" + formID + "]");
			}
			return template;
		}
	}

	/**
	 * load a template instance fresh from the original FormDef, retrieved from RMS
	 * @param formID
	 * @return
	 */
	public static FormInstance loadTemplateInstance (int formID) {
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		FormDef f = (FormDef)forms.read(formID);
		return (f != null ? f.getMainInstance() : null);
	}

	/**
	 * recursively read in a node of the instance, by filling out the template instance
	 * @param e
	 * @param ref
	 * @param in
	 * @param pf
	 * @throws IOException
	 * @throws DeserializationException
	 */
	private void readTreeElement (TreeElement e, DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		TreeElement templ = instance.getTemplatePath(e.getRef());
		boolean isGroup = !templ.isLeaf();

		if (isGroup) {
			List<String> childTypes = new ArrayList<String>(templ.getNumChildren());
			for (int i = 0; i < templ.getNumChildren(); i++) {
				String childName = templ.getChildAt(i).getName();
				if (!childTypes.contains(childName)) {
					childTypes.add(childName);
				}
			}

			for (int i = 0; i < childTypes.size(); i++) {
				String childName = childTypes.get(i);

				TreeReference childTemplRef = e.getRef().extendRef(childName, 0);
				TreeElement childTempl = instance.getTemplatePath(childTemplRef);

				boolean repeatable = childTempl.isRepeatable();
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
						TreeReference dstRef = e.getRef().extendRef(childName, j);
						try {
							instance.copyNode(childTempl, dstRef);
						} catch(InvalidReferenceException ire) {
							//If there is an invalid reference, this is a malformed instance,
							//so we'll throw a Deserialization exception.
							TreeReference r = ire.getInvalidReference();
							if(r == null) {
								throw new DeserializationException("Null Reference while attempting to deserialize! " + ire.getMessage());
							} else{
								throw new DeserializationException("Invalid Reference while attemtping to deserialize! Reference: " + r.toString(true) + " | "+ ire.getMessage());
							}

						}

						TreeElement child = e.getChild(childName, j);
						child.setRelevant(true);
						readTreeElement(child, in, pf);
					}
				} else {
					TreeElement child = e.getChild(childName, 0);
					child.setRelevant(relevant);
					if (relevant) {
						readTreeElement(child, in, pf);
					}
				}
			}
		} else {
			e.setValue((IAnswerData)ExtUtil.read(in, new ExtWrapAnswerData(e.getDataType())));
		}
	}

	/**
	 * recursively write out a node of the instance
	 * @param out
	 * @param e
	 * @param ref
	 * @throws IOException
	 */
	private void writeTreeElement (DataOutputStream out, TreeElement e) throws IOException {
		TreeElement templ = instance.getTemplatePath(e.getRef());
		boolean isGroup = !templ.isLeaf();

		if (isGroup) {
		   List<String> childTypesHandled = new ArrayList<String>(templ.getNumChildren());
			for (int i = 0; i < templ.getNumChildren(); i++) {
				String childName = templ.getChildAt(i).getName();
				if (!childTypesHandled.contains(childName)) {
					childTypesHandled.add(childName);

					int mult = e.getChildMultiplicity(childName);
					if (mult > 0 && !e.getChild(childName, 0).isRelevant()) {
						mult = 0;
					}

					ExtUtil.writeNumeric(out, mult);
					for (int j = 0; j < mult; j++) {
						writeTreeElement(out, e.getChild(childName, j));
					}
				}
			}
		} else {
			ExtUtil.write(out, new ExtWrapAnswerData(e.getDataType(), e.getValue()));
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
					if ( CHOICE_MODE == CHOICE_VALUE ) {
						val = getSelectOne(ExtUtil.read(in, String.class));
					} else {
						val = getSelectOne(ExtUtil.read(in, Integer.class));
					}
				} else if (answerType == SelectMultiData.class) {
					if ( CHOICE_MODE == CHOICE_VALUE ) {
						val = getSelectMulti((List<Object>)ExtUtil.read(in, new ExtWrapList(String.class)));
					} else {
						val = getSelectMulti((List<Object>)ExtUtil.read(in, new ExtWrapList(Integer.class)));
					}
				} else {
					switch (flag) {
					case 0x40: answerType = StringData.class; break;
					case 0x41: answerType = IntegerData.class; break;
					case 0x42: answerType = DecimalData.class; break;
					case 0x43: answerType = DateData.class; break;
					case 0x44: answerType = BooleanData.class; break;
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
						} else if (val instanceof BooleanData) {
							prefix = 0x44;
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
	 * reduce a SelectMultiData to a list of integers (index mode) or strings (value mode)
	 * @param data
	 * @return
	 */
	private List<Object> compactSelectMulti (SelectMultiData data) {
	   List<Selection> val = (List<Selection>)data.getValue();
		List<Object> choices = new ArrayList<Object>(val.size());
		for (int i = 0; i < val.size(); i++) {
			choices.add(extractSelection((Selection)val.get(i)));
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
	 * create a SelectMultiData from a list of integers (index mode) or strings (value mode)
	 */
	private SelectMultiData getSelectMulti (List<Object> v) {
	   List<Selection> choices = new ArrayList<Selection>(v.size());
		for (int i = 0; i < v.size(); i++) {
			choices.add(makeSelection(v.get(i)));
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
			return Integer.valueOf(s.index);
		default: throw new IllegalArgumentException();
		}
	}

	/**
	 * build a Selection from an integer or string, according to the current CHOICE_MODE
	 * @param o
	 * @return
	 */
	private Selection makeSelection (Object o) {
		if (o instanceof String) {
			return new Selection((String)o);
		} else if (o instanceof Integer) {
			return new Selection(((Integer)o).intValue());
		} else {
			throw new RuntimeException();
		}
	}

	public void clean() {
		// TODO Auto-generated method stub

	}

	/**
	 * map xforms data types to the Class that represents that data in a FormInstance
	 * @param dataType
	 * @return
	 */
	public static Class classForDataType (int dataType) {
		switch (dataType) {
		case Constants.DATATYPE_NULL: return StringData.class;
		case Constants.DATATYPE_TEXT: return StringData.class;
		case Constants.DATATYPE_INTEGER: return IntegerData.class;
		case Constants.DATATYPE_LONG: return LongData.class;
		case Constants.DATATYPE_DECIMAL: return DecimalData.class;
		case Constants.DATATYPE_BOOLEAN: return BooleanData.class;
		case Constants.DATATYPE_DATE: return DateData.class;
		case Constants.DATATYPE_TIME: return TimeData.class;
		case Constants.DATATYPE_DATE_TIME: return DateTimeData.class;
		case Constants.DATATYPE_CHOICE: return SelectOneData.class;
		case Constants.DATATYPE_CHOICE_LIST: return SelectMultiData.class;
		case Constants.DATATYPE_GEOPOINT: return GeoPointData.class;
		case Constants.DATATYPE_GEOSHAPE: return GeoShapeData.class;
		case Constants.DATATYPE_GEOTRACE: return GeoTraceData.class;
		default: return null;
		}
	}

}
