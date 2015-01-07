/**
 * 
 */
package org.javarosa.core.services.storage.util;

import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.DataUtil;

/**
 * @author ctsims
 *
 */
public class DummyStorageIterator<T extends Persistable> implements IStorageIterator<T> {
	Hashtable<Integer, T> data;
	int count;
	Integer[] keys;
	

	public DummyStorageIterator(Hashtable<Integer, T> data) {
		this.data = data;
		keys = new Integer[data.size()];
		int i = 0;
		for(Enumeration<Integer> en = data.keys() ;en.hasMoreElements();) {
			keys[i] = en.nextElement();
			++i;
		}
		count = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IStorageIterator#hasMore()
	 */
	public boolean hasMore() {
		return count < keys.length;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IStorageIterator#nextID()
	 */
	public int nextID() {
		count++;
		return keys[count -1].intValue(); 
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IStorageIterator#nextRecord()
	 */
	public T nextRecord() {
		return data.get(DataUtil.integer(nextID()));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IStorageIterator#numRecords()
	 */
	public int numRecords() {
		return data.size();
	}

	public int peekID() {
		return keys[count];
	}

}
