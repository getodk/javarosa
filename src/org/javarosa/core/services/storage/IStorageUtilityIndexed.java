package org.javarosa.core.services.storage;

import java.util.List;
import java.util.NoSuchElementException;

import org.javarosa.core.util.InvalidIndexException;
import org.javarosa.core.util.externalizable.Externalizable;

/* TEMPORARY / EXPERIMENTAL */

public interface IStorageUtilityIndexed<E extends Externalizable> extends IStorageUtility<E> {

	/**
	* Retrieves a List of IDs of Externalizable objects in storage for which the field
	* specified contains the value specified.
	*
	* @param fieldName The name of a field which should be evaluated
	* @param value The value which should be contained by the field specified
	* @return A List of Integers such that retrieving the Externalizable object with any
	* of those integer IDs will result in an object for which the field specified is equal
	* to the value provided.
	* @throws RuntimeException (Fix this exception type) if the field is unrecognized by the
	* meta data
	*/
    List<Integer> getIDsForValue (String fieldName, Object value);
 	/**
 	*
 	* Retrieves a Externalizable object from the storage which is reference by the unique index fieldName.
 	*
 	* @param fieldName The name of the index field which will be evaluated
 	* @param value The value which should be set in the index specified by fieldName for the returned
 	* object.
 	* @return An Externalizable object e, such that e.getMetaData(fieldName).equals(value);
 	* @throws NoSuchElementException If no objects reside in storage for which the return condition
 	* can be successful.
 	* @throws InvalidIndexException If the field used is an invalid index, because more than one field in the Storage
 	* contains the value of the index requested.
 	*/
 	E getRecordForValue (String fieldName, Object value) throws NoSuchElementException, InvalidIndexException;

 	/**
 	 * Optional. Register a new index for this storage which may optionally be able for indexed operations
 	 * going forward. This will likely take a substantial amount of time for larger storage utilities.
 	 *
 	 * @param filterIndex
 	 */
	void registerIndex(String filterIndex);

}
