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

package org.javarosa.core.model.util.restorable;

import java.util.Date;
import java.util.List;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class RestoreUtils {
	public static final String RECORD_ID_TAG = "rec-id";

	public static IXFormyFactory xfFact;

	public static TreeReference ref (String refStr) {
		return xfFact.ref(refStr);
	}

	public static TreeReference absRef (String refStr, FormInstance dm) {
		TreeReference ref = ref(refStr);
		if (!ref.isAbsolute()) {
			ref = ref.parent(topRef(dm));
		}
		return ref;
	}

	public static TreeReference topRef (FormInstance dm) {
		return ref("/" + dm.getRoot().getName());
	}

	public static TreeReference childRef (String childPath, TreeReference parentRef) {
		return ref(childPath).parent(parentRef);
	}

	private static FormInstance newDataModel (String topTag) {
		FormInstance dm = new FormInstance();
		dm.addNode(ref("/" + topTag));
		return dm;
	}

	public static FormInstance createDataModel (Restorable r) {
		FormInstance dm = newDataModel(r.getRestorableType());

		if (r instanceof Persistable) {
			addData(dm, RECORD_ID_TAG, Integer.valueOf(((Persistable)r).getID()));
		}

		return dm;
	}

	public static FormInstance createRootDataModel (Restorable r) {
		FormInstance inst = createDataModel(r);
		inst.schema = "http://openrosa.org/backup";
		addData(inst, "timestamp", new Date(), Constants.DATATYPE_DATE_TIME);
		return inst;
	}

	public static void addData (FormInstance dm, String xpath, Object data) {
		addData(dm, xpath, data, getDataType(data));
	}

	public static void addData (FormInstance dm, String xpath, Object data, int dataType) {
		if (data == null) {
			dataType = -1;
		}

		IAnswerData val;
		switch (dataType) {
		case -1: val = null; break;
		case Constants.DATATYPE_TEXT: val = new StringData((String)data); break;
		case Constants.DATATYPE_INTEGER: val = new IntegerData((Integer)data); break;
		case Constants.DATATYPE_LONG: val = new LongData((Long)data); break;
		case Constants.DATATYPE_DECIMAL: val = new DecimalData((Double)data); break;
		case Constants.DATATYPE_BOOLEAN: val = new StringData(((Boolean)data).booleanValue() ? "t" : "f"); break;
		case Constants.DATATYPE_DATE: val = new DateData((Date)data); break;
		case Constants.DATATYPE_DATE_TIME: val = new DateTimeData((Date)data); break;
		case Constants.DATATYPE_TIME: val = new TimeData((Date)data); break;
		case Constants.DATATYPE_CHOICE_LIST: val = (SelectMultiData)data; break;
		default: throw new IllegalArgumentException("Don't know how to handle data type [" + dataType + "]");
		}

		TreeReference ref = absRef(xpath, dm);
		if (dm.addNode(ref, val, dataType) == null) {
			throw new RuntimeException("error setting value during object backup [" + xpath + "]");
		}
	}

	//used for outgoing data
	public static int getDataType (Object o) {
		int dataType = -1;
		if (o instanceof String) {
			dataType = Constants.DATATYPE_TEXT;
		} else if (o instanceof Integer) {
			dataType = Constants.DATATYPE_INTEGER;
		} else if (o instanceof Long) {
			dataType = Constants.DATATYPE_LONG;
		}  else if (o instanceof Float || o instanceof Double) {
			dataType = Constants.DATATYPE_DECIMAL;
		} else if (o instanceof Date) {
			dataType = Constants.DATATYPE_DATE;
		} else if (o instanceof Boolean) {
			dataType = Constants.DATATYPE_BOOLEAN; //booleans are serialized as a literal 't'/'f'
		} else if (o instanceof SelectMultiData) {
			dataType = Constants.DATATYPE_CHOICE_LIST;
		}
		return dataType;
	}

	//used for incoming data
	public static int getDataType (Class c) {
		int dataType;
		if (c == String.class) {
			dataType = Constants.DATATYPE_TEXT;
		} else if (c == Integer.class) {
			dataType = Constants.DATATYPE_INTEGER;
		} else if (c == Long.class) {
			dataType = Constants.DATATYPE_LONG;
		} else if (c == Float.class || c == Double.class) {
			dataType = Constants.DATATYPE_DECIMAL;
		} else if (c == Date.class) {
			dataType = Constants.DATATYPE_DATE;
			//Clayton Sims - Jun 16, 2009 - How are we handling Date v. Time v. DateTime?
		} else if (c == Boolean.class) {
			dataType = Constants.DATATYPE_TEXT; //booleans are serialized as a literal 't'/'f'
		} else {
			throw new RuntimeException("Can't handle data type " + c.getName());
		}

		return dataType;
	}

	public static Object getValue (String xpath, FormInstance tree) {
		return getValue(xpath, topRef(tree), tree);
	}

	public static Object getValue (String xpath, TreeReference context, FormInstance tree) {
		TreeElement node = tree.resolveReference(ref(xpath).contextualize(context));
		if (node == null) {
			throw new RuntimeException("Could not find node [" + xpath + "] when parsing saved instance!");
		}

		if (node.isRelevant()) {
			IAnswerData val = node.getValue();
			return (val == null ? null : val.getValue());
		} else {
			return null;
		}
	}

	public static void applyDataType (FormInstance dm, String path, TreeReference parent, Class type) {
		applyDataType(dm, path, parent, getDataType(type));
	}

	public static void applyDataType (FormInstance dm, String path, TreeReference parent, int dataType) {
		TreeReference ref = childRef(path, parent);

		List<TreeReference> v = new EvaluationContext(dm).expandReference(ref);
		for (int i = 0; i < v.size(); i++) {
			TreeElement e = dm.resolveReference(v.get(i));
			e.setDataType(dataType);
		}
	}

	public static void templateChild (FormInstance dm, String prefixPath, TreeReference parent, Restorable r) {
		TreeReference childRef = (prefixPath == null ? parent : RestoreUtils.childRef(prefixPath, parent));
		childRef = childRef(r.getRestorableType(), childRef);

		templateData(r, dm, childRef);
	}

	public static void templateData (Restorable r, FormInstance dm, TreeReference parent) {
		if (parent == null) {
			parent = topRef(dm);
			applyDataType(dm, "timestamp", parent, Date.class);
		}

		if (r instanceof Persistable) {
			applyDataType(dm, RECORD_ID_TAG, parent, Integer.class);
		}

		r.templateData(dm, parent);
	}

	public static void mergeDataModel (FormInstance parent, FormInstance child, String xpathParent) {
		mergeDataModel(parent, child, absRef(xpathParent, parent));
	}

	public static void mergeDataModel (FormInstance parent, FormInstance child, TreeReference parentRef) {
		TreeElement parentNode = parent.resolveReference(parentRef);
		//ugly
		if (parentNode == null) {
			parentRef = parent.addNode(parentRef);
			parentNode = parent.resolveReference(parentRef);
		}
		TreeElement childNode = child.getRoot();

		int mult = parentNode.getChildMultiplicity(childNode.getName());
		childNode.setMult(mult);

		parentNode.addChild(childNode);
	}

	public static FormInstance exportRMS (IStorageUtility storage, Class type, String parentTag, IRecordFilter filter) {
		if (!Externalizable.class.isAssignableFrom(type) || !Restorable.class.isAssignableFrom(type)) {
			return null;
		}

		FormInstance dm = newDataModel(parentTag);

		IStorageIterator ri = storage.iterate();
		while (ri.hasMore()) {
			Object obj = ri.nextRecord();

			if (filter == null || filter.filter(obj)) {
				FormInstance objModel = ((Restorable)obj).exportData();
				mergeDataModel(dm, objModel, topRef(dm));
			}
		}

		return dm;
	}

	public static FormInstance subDataModel (TreeElement top) {
		TreeElement newTop = top.shallowCopy();
		newTop.setMult(0);
		return new FormInstance(newTop);
	}

	public static void exportRMS (FormInstance parent, Class type, String grouperName, IStorageUtility storage, IRecordFilter filter) {
		FormInstance entities = RestoreUtils.exportRMS(storage, type, grouperName, filter);
		RestoreUtils.mergeDataModel(parent, entities, ".");
	}

	public static void importRMS (FormInstance dm, IStorageUtility storage, Class type, String path) {
		if (!Externalizable.class.isAssignableFrom(type) || !Restorable.class.isAssignableFrom(type)) {
			return;
		}

		boolean idMatters = Persistable.class.isAssignableFrom(type);

		String childName = ((Restorable)PrototypeFactory.getInstance(type)).getRestorableType();
		TreeElement e = dm.resolveReference(absRef(path, dm));
		List<TreeElement> children = e.getChildrenWithName(childName);

		for (int i = 0; i < children.size(); i++) {
			FormInstance child = subDataModel(children.get(i));

			Restorable inst = (Restorable)PrototypeFactory.getInstance(type);

			//restore record id first so 'importData' has access to it
			int recID = -1;
			if (idMatters) {
				recID = ((Integer)getValue(RECORD_ID_TAG, child)).intValue();
				((Persistable)inst).setID(recID);
			}

			inst.importData(child);

			try {
				if (idMatters) {
					storage.write((Persistable)inst);
				} else {
					storage.add((Externalizable)inst);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Error importing RMS during restore! [" + type.getName() + ":" + recID + "]; " + ex.getMessage());
			}
		}
	}

	public static ByteArrayPayload dispatch (FormInstance dm) {
		return (ByteArrayPayload)xfFact.serializeInstance(dm);
	}

	public static FormInstance receive (byte[] payload, Class restorableType) {
		return xfFact.parseRestore(payload, restorableType);
	}

	public static boolean getBoolean (Object o) {
		if (o instanceof String) {
			String bool = (String)o;
			if ("t".equals(bool)) {
				return true;
			} else if ("f".equals(bool)) {
				return false;
			} else {
				throw new RuntimeException("boolean string must be t or f");
			}
		} else {
			throw new RuntimeException("booleans are encoded as strings");
		}
	}


}
