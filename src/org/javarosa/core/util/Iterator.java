/**
 * 
 */
package org.javarosa.core.util;

/**
 * Interface for iterating through a set of records from an IStorageUtility
 */
public interface Iterator<E> {

	/**
	 * NOTE: if the underlying IStorageUtility is modified while this iterator is being iterated through,
	 * any calls to nextID() or nextRecord() after the modification will throw a StorageModifiedException
	 * (this is a RuntimeException). To prevent this from happening, you can lock the entire StorageUtility
	 * *before* calling iterate(), and release the lock only after you have iterated through all records.
	 * However, this will prevent all other threads from access the StorageUtility for the entire duration
	 * of the iteration. Also, it will not protect against you modifying the storage in the same thread you
	 * are doing the iteration in.
	 * 
	 * Also, if you call nextID(), then call StorageUtility.read() yourself, there is a very slight risk that
	 * another thread will change or invalidate the record with that ID in between your call to nextID() and
	 * read(). To prevent against that, you can lock the StorageUtility before calling nextID and release it
	 * after calling read(). This risk does not exist when calling nextRecord().
	 */
	
	/**
	 * Number of records in the set
	 * 
	 * @return number of records
	 */
	int numRecords ();
	
	/**
	 * Return the ID of the next record in the set without advancing the iterator.
	 * 
	 * @return ID of next record
	 * @throws StorageModifiedException if the underlying StorageUtility has been modified since this iterator
	 * was created
	 * @throws IllegalStateException if all records have already been iterated through
	 */
	int peekID ();
	
	/**
	 * Return the ID of the next record in the set. Advance the iteration cursor by one.
	 * 
	 * @return ID of next record
	 * @throws StorageModifiedException if the underlying StorageUtility has been modified since this iterator
	 * was created
	 * @throws IllegalStateException if all records have already been iterated through
	 */
	int nextID ();
	
	/**
	 * Return the next record in the set. Advance the iteration cursor by one.
	 * 
	 * @return object representation of next record
	 * @throws IllegalStateException if all records have already been iterated through
	 */
	E nextRecord ();
	
	/**
	 * Return whether the set has more records to iterate through
	 * 
	 * @return true if there are more records to iterate though.
	 */
	boolean hasMore ();
}
