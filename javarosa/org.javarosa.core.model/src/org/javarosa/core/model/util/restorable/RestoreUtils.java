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

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class RestoreUtils {
	public static final String RECORD_ID_TAG = "rec-id";
	
	public static IXFormyFactory xfFact;
	
	public static TreeReference ref (String refStr) {
		return xfFact.ref(refStr);
	}
	
	public static TreeReference absRef (String refStr, DataModelTree dm) {
		TreeReference ref = ref(refStr);
		if (!ref.isAbsolute()) {
			ref = ref.parent(topRef(dm));
		}
		return ref;
	}
	
	public static TreeReference topRef (DataModelTree dm) {
		return ref("/" + dm.getRoot().getName());
	}
	
	public static TreeReference childRef (String childPath, TreeReference parentRef) {
		return ref(childPath).parent(parentRef);
	}
	
	private static DataModelTree newDataModel (String topTag) {
		DataModelTree dm = new DataModelTree();
		dm.addNode(ref("/" + topTag));
		return dm;
	}
	
	public static DataModelTree createDataModel (Restorable r) {
		DataModelTree dm = newDataModel(r.getRestorableType());
		
		if (r instanceof IDRecordable) {
			addData(dm, RECORD_ID_TAG, new Integer(((IDRecordable)r).getRecordId()));
		}
		
		return dm;
	}
	
	public static void addData (DataModelTree dm, String xpath, Object data) {
		addData(dm, xpath, data, getDataType(data));
	}
	
	public static void addData (DataModelTree dm, String xpath, Object data, int dataType) {
		if (data == null) {
			dataType = -1;
		}
			
		IAnswerData val;
		switch (dataType) {
		case -1: val = null; break;
		case Constants.DATATYPE_TEXT: val = new StringData((String)data); break;
		case Constants.DATATYPE_INTEGER: val = new IntegerData((Integer)data); break;
		case Constants.DATATYPE_DECIMAL: val = new DecimalData((Double)data); break;
		case Constants.DATATYPE_BOOLEAN: val = new StringData(((Boolean)data).booleanValue() ? "t" : "f"); break;
		case Constants.DATATYPE_DATE: val = new DateData((Date)data); break;
		case Constants.DATATYPE_DATE_TIME: val = new DateData((Date)data); break;
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
		} else if (o instanceof Integer || o instanceof Long) {
			dataType = Constants.DATATYPE_INTEGER;
		} else if (o instanceof Float || o instanceof Double) {
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
		} else if (c == Integer.class || c == Long.class) {
			dataType = Constants.DATATYPE_INTEGER;
		} else if (c == Float.class || c == Double.class) {
			dataType = Constants.DATATYPE_DECIMAL;
		} else if (c == Date.class) {
			dataType = Constants.DATATYPE_DATE;
		} else if (c == Boolean.class) {
			dataType = Constants.DATATYPE_TEXT; //booleans are serialized as a literal 't'/'f'
		} else {
			throw new RuntimeException("Can't handle data type " + c.getName());
		}
		
		return dataType;
	}
	
	public static Object getValue (String xpath, DataModelTree tree) {
		return getValue(xpath, topRef(tree), tree);
	}
	
	public static Object getValue (String xpath, TreeReference context, DataModelTree tree) {
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
	
	public static void applyDataType (DataModelTree dm, String path, TreeReference parent, Class type) {
		applyDataType(dm, path, parent, getDataType(type));
	}
	
	public static void applyDataType (DataModelTree dm, String path, TreeReference parent, int dataType) {
		TreeReference ref = childRef(path, parent);
		
		Vector v = dm.expandReference(ref);
		for (int i = 0; i < v.size(); i++) {
			TreeElement e = dm.resolveReference((TreeReference)v.elementAt(i));
			e.dataType = dataType;
		}
	}
	
	public static void templateChild (DataModelTree dm, String prefixPath, TreeReference parent, Restorable r) {	
		TreeReference childRef = (prefixPath == null ? parent : RestoreUtils.childRef(prefixPath, parent));
		childRef = childRef(r.getRestorableType(), childRef);
		
		templateData(r, dm, childRef);
	}
	
	public static void templateData (Restorable r, DataModelTree dm, TreeReference parent) {
		if (parent == null)
			parent = topRef(dm);
		
		if (r instanceof IDRecordable) {
			applyDataType(dm, RECORD_ID_TAG, parent, Integer.class);
		}
		
		r.templateData(dm, parent);
	}
	
	public static void mergeDataModel (DataModelTree parent, DataModelTree child, String xpathParent) {
		mergeDataModel(parent, child, absRef(xpathParent, parent));
	}
	
	public static void mergeDataModel (DataModelTree parent, DataModelTree child, TreeReference parentRef) {
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
	
	public static DataModelTree exportRMS (RMSUtility rmsu, Class type, String parentTag, IRecordFilter filter) {
		if (!Externalizable.class.isAssignableFrom(type) || !Restorable.class.isAssignableFrom(type)) {
			return null;
		}
				
		DataModelTree dm = newDataModel(parentTag);
		
		IRecordStoreEnumeration re = rmsu.enumerateRecords();
		while (re.hasNextElement()) {
			int recID = -1;
			try {
				recID = re.nextRecordId();
			} catch (RecordStorageException e) {
				System.err.println("grrr");
			}
			
			Object obj = PrototypeFactory.getInstance(type);
			
			try {
				rmsu.retrieveFromRMS(recID, (Externalizable)obj);
			} catch (IOException e) {
				System.err.println("glaven");
			} catch (DeserializationException e) {
				System.err.println("grief");
			}
			
			if (filter == null || filter.filter(obj)) {
				DataModelTree objModel = ((Restorable)obj).exportData();
				mergeDataModel(dm, objModel, topRef(dm));
			}
		}
		
		return dm;
	}
	
	public static DataModelTree subDataModel (TreeElement top) {
		TreeElement newTop = top.shallowCopy();
		newTop.setMult(0);
		return new DataModelTree(newTop);
	}
	
	public static void exportRMS (DataModelTree parent, Class type, String grouperName, RMSUtility rmsu, IRecordFilter filter) {
		DataModelTree entities = RestoreUtils.exportRMS(rmsu, type, grouperName, filter);
		RestoreUtils.mergeDataModel(parent, entities, ".");
	}
	
	public static void importRMS (DataModelTree dm, RMSUtility rmsu, Class type, String path) {
		if (!Externalizable.class.isAssignableFrom(type) || !Restorable.class.isAssignableFrom(type)) {
			return;
		}

		boolean idMatters = IDRecordable.class.isAssignableFrom(type);
		
		String childName = ((Restorable)PrototypeFactory.getInstance(type)).getRestorableType();
		TreeElement e = dm.resolveReference(absRef(path, dm));
		Vector children = e.getChildrenWithName(childName);
		
		if (idMatters) {
			//should we check if RMS is empty? this presents problems for users, where the admin user is already defined
			
			Vector recIDs = new Vector();
			for (int i = 0; i < children.size(); i++) {
				int recID = ((Integer)((TreeElement)children.elementAt(i)).getChild(RECORD_ID_TAG, 0).getValue().getValue()).intValue();
				recIDs.addElement(new Integer(recID));
			}
			
			if (!rmsu.makeIDsAvailable(recIDs)) {
				throw new RuntimeException("Cannot restore RMS using same IDs!");
			}
		}
		
		for (int i = 0; i < children.size(); i++) {
			DataModelTree child = subDataModel((TreeElement)children.elementAt(i));
		
			Restorable inst = (Restorable)PrototypeFactory.getInstance(type);
			
			//restore record id first so 'importData' has access to it
			if (idMatters) {
				((IDRecordable)inst).setRecordId(((Integer)getValue(RECORD_ID_TAG, child)).intValue());
			}
			
			inst.importData(child);
			
			if (idMatters) {
				IDRecordable instRec = (IDRecordable)inst;
				if (!rmsu.writeToRMSusingID(instRec)) {
					throw new RuntimeException("Error importing RMS during restore! [" + rmsu.getName() + ":" + instRec.getRecordId() + "]");
				}
			} else {
				rmsu.writeToRMS(inst, rmsu.newMetaData(inst));
			}
		}
	}
	
	public static ByteArrayPayload dispatch (DataModelTree dm) {
		return (ByteArrayPayload)xfFact.serializeModel(dm);
	}
	
	public static DataModelTree receive (byte[] payload, Class restorableType) {
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
