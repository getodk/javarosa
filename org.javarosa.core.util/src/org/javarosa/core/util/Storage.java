package org.javarosa.core.util;

import java.util.Vector;

import org.javarosa.core.util.db.Persistent;
import org.javarosa.core.util.db.Record;

/**
 * An interface that all concrete storages should implement. This interface shields 
 * the user from the underlying storage type.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface Storage {
	
	/** Deletes all records from the storage. */
	public void delete();
	
	/**
	 * Deletes a record from the data store.
	 * 
	 * @param recId - the numeric identifier of the record to be deleted.
	 */
	public void delete(int recId);
	
	/** 
	 * Reads a list of objects of a given class from persistent storage.
	 * 
	 * @param cls - the class of the object to be retrieved.
	 * @return - the list of objects retrieved.
	 */
	public Vector read(Class cls);
	
	/** 
	 * Reads an object from persistent store 
	 * using its numeric unique identifier and class.
	 * 
	 * @param id - the unique identifier of the object.
	 * @param cls - the class of the object.
	 * @return the object
	 */
	public Object read(int id,Class cls);
	
	/** 
	 * Reads the first object from persistent store
	 * 
	 * @param cls - the class of the object.
	 * @return the object
	 */
	public Persistent readFirst(Class cls);
	
	/** 
	 * Saves a persistent object to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param obj - the object to save.
	 * @return - the unique identifier of the saved object. 
	 * This identifier can be used to later on retrieve this particular object form persistent storage.
	 */
	public int addNew(Persistent obj);
	
	/** 
	 * updates a persistent object in storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param id - the recordid of the object to save.
	 * @param obj - the object to save.
	 */
	public void update(int id,Persistent obj);
	
	/** 
	 * Saves a list of persistent objects to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param persistentObjects - the list of objects to save.
	 * @return - the unique identifiers of the saved object. 
	 * These identifiers can be used to later on retrieve these particular objects form persistent storage.
	 */
	public Vector addNew(Vector persistentObjects);
	
	/**
	 * Saves a record to the data store.
	 * 
	 * @param rec
	 */
	public void save(Record rec);
	
	/**
	 * Deletes a record from the data store.
	 * 
	 * @param rec
	 */
	public void delete(Record rec);
}
