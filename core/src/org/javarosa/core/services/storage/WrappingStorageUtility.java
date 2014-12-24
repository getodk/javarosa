package org.javarosa.core.services.storage;

import java.util.List;
import java.util.NoSuchElementException;

import org.javarosa.core.util.InvalidIndexException;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * A wrapper implementation of IStorageUtility that lets you serialize an object with a serialization
 * scheme other than the default scheme provided by the object's readExternal/writeExternal methods.
 *
 * For example, FormInstance contains lots of redundant information about the structure of the instance
 * which doesn't change among saved instances. The extra space used for this redundant info can seriously
 * limit the number of saved forms we can store on a device. We can use this utility to serialize
 * FormInstances in a different way that excludes this redundant info (meaning we have to take the more
 * complicated step of restoring it from elsewhere during deserialization), with the benefit of much
 * smaller record sizes.
 *
 * The alternate scheme is provided via a wrapper object, which accepts the base object and whose
 * readExternal/writeExternal methods implement the new scheme.
 *
 * All methods pass through to an underlying StorageUtility; you may get warnings about type mismatches
 *
 * @author Drew Roos
 *
 */
public class WrappingStorageUtility implements IStorageUtilityIndexed {
	IStorageUtility storage;		/* underlying StorageUtility */
	SerializationWrapper wrapper;   /* wrapper that defines the alternate serialization scheme; the wrapper is set once for
	                                 * the life of the StorageUtility and is re-used all read and write calls
	                                 */

	/**
	 * Defines an alternate serialization scheme. The alternate scheme is implemented in this class's
	 * readExternal and writeExternal methods.
	 *
	 * (kind of like ExternalizableWrapper -- but not quite a drop-in replacement)
	 */
	public interface SerializationWrapper extends Externalizable {
		/**
		 * set the underlying object (to be followed by a call to writeExternal)
		 * @param e
		 */
		void setData (Externalizable e);

		/**
		 * retrieve the underlying object (to be followed by a call to readExternal)
		 * @return
		 */
		Externalizable getData ();

		/**
		 * return type of underlying object
		 * @return
		 */
		Class baseType ();

		void clean();
	}

	/**
	 * Create a new wrapping StorageUtility

	 * @param name unique name for underlying StorageUtility
	 * @param wrapper serialization wrapper
	 * @param storageFactory factory to create underlying StorageUtility
	 */
	public WrappingStorageUtility (String name, SerializationWrapper wrapper, IStorageFactory storageFactory) {
		this.storage = storageFactory.newStorage(name, wrapper.getClass());
		this.wrapper = wrapper;
	}

	public Externalizable read(int id) {
		return ((SerializationWrapper)storage.read(id)).getData();
	}

	public void write(final Persistable p) throws StorageFullException {
		synchronized(wrapper) {
			wrapper.setData(p);
			if(wrapper instanceof IMetaData) {
				storage.write(new FauxIndexedPersistable(p, wrapper, (IMetaData)wrapper));
			} else {
				storage.write(new FauxIndexedPersistable(p, wrapper));
			}
			wrapper.clean();
		}
	}


	public int add(Externalizable e) throws StorageFullException {
		synchronized(wrapper) {
			wrapper.setData(e);
			int result = storage.add(wrapper);
			wrapper.clean();
			return result;
		}
	}

	public void update(int id, Externalizable e) throws StorageFullException {
		synchronized(wrapper) {
			wrapper.setData(e);
			storage.update(id, wrapper);
			wrapper.clean();
		}
	}

	public IStorageIterator iterate() {
		return new IStorageIterator () {
			IStorageIterator baseIterator = storage.iterate();
			public boolean hasMore() {
				return baseIterator.hasMore();
			}

			public int nextID() {
				return baseIterator.nextID();
			}

			public Externalizable nextRecord() {
				return ((SerializationWrapper)baseIterator.nextRecord()).getData();
			}

			public int numRecords() {
				return baseIterator.numRecords();
			}

			public int peekID() {
				return baseIterator.peekID();
			}
		};
	}

	/* pass-through methods */

	public byte[] readBytes(int id) {
		return storage.readBytes(id);
	}

	public void remove(int id) {
		storage.remove(id);
	}

	public void remove(Persistable p) {
		storage.remove(p);
	}

	public void removeAll() {
		storage.removeAll();
	}

	public List<Integer> removeAll(EntityFilter ef) {
		return storage.removeAll(ef);
	}

	public boolean exists(int id) {
		return storage.exists(id);
	}

	public boolean isEmpty() {
		return storage.isEmpty();
	}

	public int getNumRecords() {
		return storage.getNumRecords();
	}

	public int getRecordSize(int id) {
		return storage.getRecordSize(id);
	}

	public int getTotalSize() {
		return storage.getTotalSize();
	}

	public void close() {
		storage.close();
	}

	public void destroy() {
		storage.destroy();
	}

	public void repack() {
		storage.repack();
	}

	public void repair() {
		storage.repair();
	}

	public Object getAccessLock() {
		return storage.getAccessLock();
	}

	public List<Integer> getIDsForValue(String fieldName, Object value) {
		return indexedStorage().getIDsForValue(fieldName, value);
	}

	public Externalizable getRecordForValue(String fieldName, Object value)
			throws NoSuchElementException, InvalidIndexException {
		return ((SerializationWrapper)indexedStorage().getRecordForValue(fieldName, value)).getData();
	}

	private IStorageUtilityIndexed indexedStorage() {
		if(!(storage instanceof IStorageUtilityIndexed)) {
			throw new RuntimeException("WrappingStorageUtility's factory is not of an indexed type, but had indexed operations requested. Please implement storage " + storage.getClass().getName() + " as indexed storage");
		}
		return (IStorageUtilityIndexed)storage;
	}

	public void setReadOnly() {
		storage.setReadOnly();
	}

	public void registerIndex(String filterIndex) {
		indexedStorage().registerIndex(filterIndex);
	}

}
