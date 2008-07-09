package org.javarosa.core.util;


import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.util.db.Serializer;
import org.javarosa.core.util.db.Persistent;
import org.javarosa.core.util.db.Record;

//TODO Exceptions in this class should be propagated to the user in some way.

/**
 * Handles storage and retrieval of objects of various types to RMS.
 * As for now, a data storage of a given name corresponds to a table
 * in the database world. The table fields would be the object fields.
 * 
 * @author Daniel Kayiwa
 *
 */
public class RMSStorage implements Storage{
	
	/** A reference to the RecordStore. */
	private RecordStore recStore;
	
	/** The name of this storage. 
	 * This name should be unique for this type of objects throught the midlet.
	 * For now, this is the name of the package and class of object type stored.
	 */
	private String name;
	
	/** Reference to the event listener. */
	private StorageListener eventListener;
	
	/** Flag to keep track of whether data store is open or closed. */
	private boolean open;

	/** 
	 * Constructs a data store with a given name. 
	 * 
	 * @param name - the name of this data store.
	 * @param eventListener - the event listener.
	 */
	public RMSStorage(String name,StorageListener eventListener){
		this.name = name;
		this.eventListener = eventListener;
	}
	
	public boolean isOpen() {
		return open;
	}

	private void setOpen(boolean open) {
		this.open = open;
	}
	
	/**
	 * Opens the data storage.
	 * 
	 * @return - true if successfully opened, else false.
	 */
	private boolean open(){
		try{
			recStore = RecordStore.openRecordStore(name, true);
			setOpen(true);
			return true;
		}
		catch(RecordStoreNotFoundException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("recordstore not found: ", e);
			System.err.println("recordstore not found: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("recordstore exception: ", e);
			System.err.println("recordstore exception: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception: ", e);
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Closes the data storage.
	 * 
	 * @return - true if successfully closed, else false.
	 */
	private boolean close(){
		try{
			recStore.closeRecordStore();
			setOpen(false);
			return true;
		}
		catch(RecordStoreNotFoundException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("recordstore not found: Exception: ", e);
			System.err.println("recordstore not found: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("recordstore Exception: ", e);
			System.err.println("recordstore exception: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception: ", e);
			e.printStackTrace();
		}
		
		return false;
	}
	
	/** Deletes all records from the data store. */
	public void delete(){
		try{
			RecordStore.deleteRecordStore(name);
		}
		catch(RecordStoreException e){
			/*if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			e.printStackTrace();*/
		}
		catch(Exception e){
			/*if(this.eventListener != null)
				this.eventListener.errorOccured("Exception e: ", e);
			System.err.println("Exception e : " + e.getMessage());
			e.printStackTrace();*/
		}
	}
	
	/**
	 * Deletes a record from the data store.
	 * 
	 * @param recId - the numeric identifier of the record to be deleted.
	 */
	public void delete(int recId){
		try{
			open();
			this.recStore.deleteRecord(recId);
		}
		catch(InvalidRecordIDException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InvalidRecordIDException: ", e);
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception e: ", e);
			System.err.println("Exception e : " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			close();
		}
	}
	
	/** 
	 * Reads a list of objects of a given class from persistent storage.
	 * 
	 * @param cls - the class of the object to be retrieved.
	 * @return - the list of objects retrieved.
	 */
	public Vector read(Class cls){
		try{
			Vector list = null;
			if(open()){	
				if(recStore.getNumRecords() > 0)
					list = new Vector();
				
				RecordEnumeration recEnum = recStore.enumerateRecords(null, null, true);
				while(recEnum.hasNextElement()){
					int id = recEnum.nextRecordId();
					Object obj = Serializer.deserialize(recStore.getRecord(id),cls);
					if(obj instanceof Record)
						((Record)obj).setRecordId(id);
					list.addElement(obj);
				}
			}
			
			return list;
		}
		catch(InvalidRecordIDException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InvalidRecordIDException: ", e);
			System.err.println("InvalidRecordIDException: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreNotOpenException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotOpenException: ", e);
			System.err.println("RecordStoreNotOpenException : " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			System.err.println("RecordStoreException : " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(IllegalAccessException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IllegalAccessException: ", e);
			System.err.println("IllegalAccessException : " +e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IOException exception: ", e);
			System.err.println("IOException exception: " +e.getMessage());
			e.printStackTrace();
		}
		catch(InstantiationException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InstantiationException: ", e);
			System.err.println("InstantiationException : " + e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception: ", e);
			System.err.println("Exception : " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			close();
		}
		
		return null;
	}
	
	/** 
	 * Reads an object from persistent store 
	 * using its numeric unique identifier and class.
	 * 
	 * @param id - the unique identifier of the object.
	 * @param cls - the class of the object.
	 * @return
	 */
	public Object read(int id,Class cls){
		try{
			open();
			Object obj = Serializer.deserialize(recStore.getRecord(id), cls);
			if(obj instanceof Record)
				((Record)obj).setRecordId(id);
			return obj;
		}
		catch(InvalidRecordIDException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InvalidRecordIDException: ", e);
			System.err.println("InvalidRecordIDException: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreNotOpenException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotOpenException: ", e);
			System.err.println("RecordStoreNotOpenException : " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			System.err.println("RecordStoreException : " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(IllegalAccessException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IllegalAccessException: ", e);
			System.err.println("IllegalAccessException : " +e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IOException exception: ", e);
			System.err.println("IOException exception: " +e.getMessage());
			e.printStackTrace();
		}
		catch(InstantiationException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InstantiationException: ", e);
			System.err.println("InstantiationException : " + e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception e: ", e);
			System.err.println("Exception e : " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			close();
		}
		return null;
	}
	
	/** 
	 * Saves a persistent object to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param obj - the object to save.
	 * @return - the unique identifier of the saved object. 
	 * This identifier can be used to later on retrieve this particular object form persistent storage.
	 */
	public int addNew(Persistent obj){
		
		try{
			open();			
			byte[] record = Serializer.serialize(obj);
			return this.recStore.addRecord(record, 0, record.length);
		}
		catch(RecordStoreFullException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreFullException: ", e);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		catch(RecordStoreNotFoundException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotFoundException: ", e);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		catch(RecordStoreNotOpenException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotOpenException: ", e);
			System.out.println(e);
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			System.out.println(e);
			e.printStackTrace();
		}
		catch(IOException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IOException: ", e);
			System.err.println(e);
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception e: ", e);
			System.err.println("Exception e : " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			close();
		}
		
		return 0;
	}
	
	/** 
	 * Updates an existing persistent object in storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param id - the recordid of the record to update.
	 * @param obj - the object to save.
	 */
	public void update(int id, Persistent obj){
		
		try{
			open();
			byte[] record = Serializer.serialize(obj);
			this.recStore.setRecord(id,record, 0, record.length);
		}
		catch(RecordStoreFullException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreFullException: ", e);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		catch(RecordStoreNotFoundException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotFoundException: ", e);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		catch(RecordStoreNotOpenException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotOpenException: ", e);
			System.out.println(e);
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			System.out.println(e);
			e.printStackTrace();
		}
		catch(IOException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IOException: ", e);
			System.err.println(e);
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception e: ", e);
			System.err.println("Exception e : " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			close();
		}
	}
	
	
	/** 
	 * Saves a list of persistent objects to storage. 
	 * A peristent object is one which implements the Persistent interface.
	 * 
	 * @param persistentObjects - the list of objects to save.
	 * @return - the unique identifiers of the saved object. 
	 * These identifiers can be used to later on retrieve these particular objects form persistent storage.
	 */
	public Vector addNew(Vector persistentObjects){
		
		Vector ret = new Vector();
		for(int i=0; i<persistentObjects.size(); i++)
			ret.addElement(new Integer(addNew((Persistent)persistentObjects.elementAt(i))));
		
		return ret;
	}
	
	public void save(Record rec){
		if(rec.isNew())
			rec.setRecordId(addNew(rec));
		else
			update(rec.getRecordId(), rec);
	}
	
	public void delete(Record rec){
		delete(rec.getRecordId());
	}
	
	public Persistent readFirst(Class cls){
		try{
			Persistent persistent = null;
			if(open()){	
				int recId = recStore.getNextRecordID();
				if(recId > 0)
					persistent = Serializer.deserialize(recStore.getRecord(recId),cls);
			}
			
			return persistent;
		}
		catch(InvalidRecordIDException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InvalidRecordIDException: ", e);
			System.err.println("InvalidRecordIDException: " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreNotOpenException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreNotOpenException: ", e);
			System.err.println("RecordStoreNotOpenException : " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(RecordStoreException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("RecordStoreException: ", e);
			System.err.println("RecordStoreException : " + e.getMessage()); // just for now
			e.printStackTrace();
		}
		catch(IllegalAccessException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IllegalAccessException: ", e);
			System.err.println("IllegalAccessException : " +e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("IOException exception: ", e);
			System.err.println("IOException exception: " +e.getMessage());
			e.printStackTrace();
		}
		catch(InstantiationException e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("InstantiationException: ", e);
			System.err.println("InstantiationException : " + e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e){
			if(this.eventListener != null)
				this.eventListener.errorOccured("Exception: ", e);
			System.err.println("Exception : " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			close();
		}
		
		return null;
	}
}
