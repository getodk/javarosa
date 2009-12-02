package org.javarosa.j2me.storage.rms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.javarosa.core.services.storage.IMetaData;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.util.InvalidIndexException;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/* TEMPORARY / EXPERIMENTAL */

public class RMSStorageUtilityIndexed extends RMSStorageUtility implements IStorageUtilityIndexed {

	Hashtable metaDataIndex = null;
	boolean hasMetaData;
	IMetaData proto;
	
	public RMSStorageUtilityIndexed (String basename, Class type) {
		super(basename, type);
		init(type);
	}

	public RMSStorageUtilityIndexed (String basename, Class type, boolean allocateIDs) {
		super(basename, type, allocateIDs);
		init(type);
	}

	private void init (Class type) {
		hasMetaData = IMetaData.class.isAssignableFrom(type);
		if (hasMetaData)
			proto = (IMetaData)PrototypeFactory.getInstance(type);
	}	
	
	private void checkIndex () {
		if (metaDataIndex == null) {
			buildIndex();
		}
	}
	
	private void buildIndex () {
		metaDataIndex = new Hashtable();
		
		if (!hasMetaData)
			return;
		
		String[] fields = proto.getMetaDataFields();
		for (int k = 0; k < fields.length; k++) {
			metaDataIndex.put(fields[k], new Hashtable());
		}
		
		IStorageIterator i = iterate();
		while (i.hasMore()) {
			int id = i.nextID();
			IMetaData obj = (IMetaData)read(id);
			indexMetaData(id, obj);
		}
	}
	
	private void indexMetaData (int id, IMetaData obj) {
		Hashtable vals = obj.getMetaData();
		for (Enumeration e = vals.keys(); e.hasMoreElements(); ) {
			String field = (String)e.nextElement();
			Object val = vals.get(field);
			
			Vector IDs = getIDList(field, val);
			if (IDs.contains(new Integer(id))) {
				System.out.println("warning: don't think this should happen [add] [" + id + ":" + field + ":" + val.toString() + "]");
			}
			IDs.addElement(new Integer(id));
		}
	}
	
	private void removeMetaData (int id, IMetaData obj) {
		Hashtable vals = obj.getMetaData();
		for (Enumeration e = vals.keys(); e.hasMoreElements(); ) {
			String field = (String)e.nextElement();
			Object val = vals.get(field);
			
			Vector IDs = getIDList(field, val);
			if (!IDs.contains(new Integer(id))) {
				System.out.println("warning: don't think this should happen [remove] [" + id + ":" + field + ":" + val.toString() + "]");
			}
			IDs.removeElement(new Integer(id));
			if (IDs.size() == 0) {
				((Hashtable)(metaDataIndex.get(field))).remove(val);
			}
		}
	}
	
	private Vector getIDList (String field, Object value) {
		Vector IDs = (Vector)((Hashtable)(metaDataIndex.get(field))).get(value);
		if (IDs == null) {
			IDs = new Vector();
			((Hashtable)(metaDataIndex.get(field))).put(value, IDs);
		}
		return IDs;
	}
	
	public void write (Persistable p) throws StorageFullException {
		IMetaData old = null;
		if (hasMetaData) {
			checkIndex();
			if (exists(p.getID()))
				old = (IMetaData)read(p.getID());
		}
		
		super.write(p);
		
		if (hasMetaData) {
			if (old != null)
				removeMetaData(p.getID(), (IMetaData)old);
			indexMetaData(p.getID(), (IMetaData)p);
		}
	}
	
	public int add (Externalizable e) throws StorageFullException {
		if (hasMetaData)
			checkIndex();

		int id = super.add(e);
		
		if (hasMetaData)
			indexMetaData(id, (IMetaData)e);
		
		return id;
	}
	
	public void update (int id, Externalizable e) throws StorageFullException {
		Externalizable old;
		if (hasMetaData) {
			old = read(id);
			checkIndex();
			removeMetaData(id, (IMetaData)old);
		}
		
		super.update(id, e);
		
		if (hasMetaData)
			indexMetaData(id, (IMetaData)e);
	}
	
	public void remove (int id) {
		Externalizable old = null;
		if (hasMetaData) {
			old = read(id);
			checkIndex();
		}
			
		super.remove(id);
		
		if (hasMetaData)
			removeMetaData(id, (IMetaData)old);
	}
	
	public Vector getIDsForValue (String fieldName, Object value) {
		checkIndex();

		Hashtable index = (Hashtable)metaDataIndex.get(fieldName);
		if (index == null) {
			throw new RuntimeException("field [" + fieldName + "] not recognized");
		}
		
		Vector IDs = (Vector)index.get(value);
		return (IDs == null ? new Vector(): IDs);
	}
	
	public Externalizable getRecordForValue (String fieldName, Object value) throws NoSuchElementException {
		Vector IDs = getIDsForValue(fieldName, value);
		if (IDs.size() == 1) {
			return read(((Integer)IDs.elementAt(0)).intValue());
		} else if (IDs.size() == 0){
			throw new NoSuchElementException("Storage utility " + getName() +  " does not contain any records with the index " + fieldName + " equal to " + value.toString());
		} else {
			throw new InvalidIndexException(fieldName + " is not a valid unique index. More than one record was found with value [" + value.toString() + "] in field [" + fieldName + "]",fieldName);
		}
	}
}

