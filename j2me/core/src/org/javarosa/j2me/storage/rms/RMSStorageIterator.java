package org.javarosa.j2me.storage.rms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.StorageModifiedException;
import org.javarosa.core.util.SortedIntSet;
import org.javarosa.core.util.externalizable.Externalizable;

public class RMSStorageIterator implements IStorageIterator {
	private RMSStorageUtility store;
	private Vector IDs;
	private int pos;
	private boolean valid;
	private Hashtable index;
	
	public RMSStorageIterator (RMSStorageUtility store, Hashtable index) {
		
		SortedIntSet IDs = new SortedIntSet();

		for (Enumeration e = index.keys(); e.hasMoreElements(); ) {
			IDs.add(((Integer)e.nextElement()).intValue());
		}
		this.index = index;
		this.store = store;
		this.IDs = IDs.getVector();
		pos = 0;
		valid = true;
	}
	
	public int numRecords () {
		return IDs.size();
	}
	
	public synchronized boolean hasMore () {
		return pos < numRecords();
	}
	

	public int peekID() {
		if (!hasMore()) {
			throw new IllegalStateException("All records have been iterated through");
		}
			
		if (!valid) {
			throw new StorageModifiedException();
		}
		return ((Integer)IDs.elementAt(pos)).intValue();
	}

	/* Note: StorageUtility lock must always be acquire before local lock, to avoid deadlock scenarios. */
	
	public int nextID () {
		synchronized (store.getAccessLock()) {
			synchronized (this) {
				if (!hasMore()) {
					throw new IllegalStateException("All records have been iterated through");
				}
					
				if (!valid) {
					throw new StorageModifiedException();
				}

				int id = ((Integer)IDs.elementAt(pos)).intValue();
				pos++;

				if (!hasMore()) {
					store.iteratorComplete(this);
				}
				
				return id;
			}
		}
	}
		
	public Externalizable nextRecord () {
		synchronized (store.getAccessLock()) {
			synchronized (this) {
				return store.read(nextID(), index);
			}
		}
	}
	
	public synchronized void invalidate () {
		valid = false;
	}
}
