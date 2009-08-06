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

package org.javarosa.core.services.storage.utilities;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;


/**
 * RMS Utilities are responsible for the persistent storage
 * of serialized data objects. The utility opens connections
 * to RMS storage, writes and retrieves records based on integer
 * Id's, and closes the connection
 * 
 * @author Munier
 *
 */
public class RMSUtility
{
    public static final int RMS_TYPE_STANDARD = 0;
    public static final int RMS_TYPE_META_DATA = 1;
    /** Creates a new instance of RMSUtility */
    protected String RS_NAME = "";
    private int iType = RMSUtility.RMS_TYPE_STANDARD;
    protected RMSUtility metaDataRMS;
    private IRecordStorage recordStore = null;

    /**
     * Constructs a new RMS Utility
     * 
     * @param name The unique name of this Utility
     * @param iType Whether this utility is a standard or metadata utility
     */
    public RMSUtility(String name, int iType)
    {
        this.RS_NAME = name;
        this.iType = iType;
        if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
		//#if debug.output==verbose
        System.out.println("RMS SIZE (" + this.RS_NAME + ") : " + this.getNumberOfRecords());
        //#endif
    }

    /**
     * Gets the unique name of this utility
     * 
     * @return The unique name for this RMS utility
     */
    public String getName()
    {
        return this.RS_NAME;
    }
    
    protected IRecordStorage getRecordStore() {
    	return recordStore;
    }

    /**
     * Opens the record store on the device.
     */
    public void open()
    {
        if (this.recordStore == null)
        {
            try
            {
            	this.recordStore = JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getRecordStoreFactory().produceNewStore();
            	this.recordStore.openAsRecordStorage(RS_NAME, true);
            }
            catch (RecordStorageException rse)
            {
                rse.printStackTrace();
            }
        }
    }

    /**
     * Closes the connection to the record store on the device
     */
    public void close()
    {
        if (this.recordStore != null)
        {
            try
            {
                this.recordStore.closeRecordStore();
                if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        		{
        			this.metaDataRMS.close();
        		}
            }
            catch (RecordStorageException rse)
            {
                rse.printStackTrace();
            }
            finally
            {
                this.recordStore = null;
            }
        }
    }

    /**
     * Writes the given object to the RMS, along with writing the 
     * given metadata object to its respective RMS
     * 
     * @param obj The Externalizable object to be written
     * @param metaDataObject The meta data descriptor for the given object
     * @return record ID of new record
     */
    public int writeToRMS(Object obj,
                           MetaDataObject metaDataObject)
    {
    	int recordId = -1;
        try
        {
        	//if this is a metadata object, we just wipe out the data object's record id that
        	//was already written in here. let's hope they're the same...
        	recordId = this.recordStore.getNextRecordID();
    		if (obj instanceof IDRecordable)
    			((IDRecordable)obj).setRecordId(recordId);
    		
            byte[] data = ExtUtil.serialize(obj);
            
            //LOG
            this.recordStore.addRecord(data, 0, data.length);
            if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
            {
                metaDataObject.setRecordId(recordId);
                metaDataObject.setSize(data.length);
                metaDataObject.setMetaDataParameters(obj);
                this.metaDataRMS.writeToRMS(metaDataObject, null);
            }
        }
        catch (RecordStorageException rse)
        {
            rse.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return recordId;
    }
    
    public boolean writeToRMSusingID (IDRecordable obj) {
    	int recID = obj.getRecordId();
    	if (recID <= 0) {
    		return false;
    	}
    	
    	try {
	    	if (retrieveByteDataFromRMS(recID) != null) {
	    		return false;
	    	}
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
	    
    	updateToRMS(recID, obj, newMetaData(obj));
    	return true;
    }
    
    /**
     * Updates the given record in the RMS, along with its metadata
     * object.
     * 
     * @param recordId The record ID for the given object
     * @param obj The Externalizable object associated wtih recordId
     * @param metaDataObject The meta data descriptor for the object
     */
    public void updateToRMS(int recordId, Object obj,
    		MetaDataObject metaDataObject)
    {
    	try
    	{
    		if (obj instanceof IDRecordable)
    			((IDRecordable)obj).setRecordId(recordId);
    		byte[] data = ExtUtil.serialize(obj);
    		this.recordStore.setRecord(recordId, data, 0, data.length);
    		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
    		{
    			metaDataObject.setRecordId(recordId);
    			metaDataObject.setSize(data.length);
    			metaDataObject.setMetaDataParameters(obj);
    			this.metaDataRMS.updateToRMS(recordId, metaDataObject, null);
    		}
    	}
    	catch (RecordStorageException rse)
    	{
    		rse.printStackTrace();
    	}
    	catch (IOException ioe)
    	{
    		ioe.printStackTrace();
    	}
    }
    
    /**
     * Writes a block of data bytes to the rms.
     * 
     * @param data The block of data to be written
     * @param metaDataObject The meta data descriptor for the data block
     */
    public void writeBytesToRMS(byte [] data, MetaDataObject metaDataObject)
    {
    	int metaID = -1;
    	try
    	{
    		int recordId = this.recordStore.getNextRecordID();
    		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
    		{
    			metaDataObject.setRecordId(recordId);
    			metaDataObject.setSize(data.length);
    			metaID = this.metaDataRMS.writeToRMS(metaDataObject, null);
    		}
    	}
    	catch (RecordStorageException rse)
    	{
    		rse.printStackTrace();
    		//TODO: Replace this with a CHECKED exception
    		throw new RuntimeException("Error Writing Meta Data record to RMS for a block of data");
    	}
    	
    	try
    	{
    		this.recordStore.addRecord(data, 0, data.length);
    	}
    	catch (RecordStorageException rse)
    	{
    		rse.printStackTrace();
    		
    		//Roll Back the Meta Data entry, since this one couldn't be written.
    		this.metaDataRMS.deleteRecord(metaID);
    		
    		//TODO: Replace this with a CHECKED exception
    		throw new RuntimeException("Error Writing Record to RMS for a block of data");
    	}
    }

    /**
     * Removes a record from persistent storage
     * 
     * @param recordId The Id of the record to be removed
     */
    public void deleteRecord(int recordId)
    {
        try
        {
            this.recordStore.deleteRecord(recordId);
            if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
            {
                this.metaDataRMS.deleteRecord(recordId);
            }
        }
        catch (RecordStorageException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Removes this RecordStore, and its associated MetaData RecordStore
     * from persistent storage.
     */
    public void delete()
    {
        try
        {   
        	if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        	{
        		this.metaDataRMS.delete();
        	}
        	recordStore.deleteRecordStore();
        	
        }
        catch (RecordStorageException e)
        {
    		//#if debug.output==verbose || debug.output==exception
            e.printStackTrace();
            //#endif
        }
    }

    /**
     * Retrieves the record associated with the given record ID, and stores
     * it in hte given object
     * @param recordId The record Id for the record to be returned
     * @param externalizableObject The object in which the deserialzed record
     * will be stored
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void retrieveFromRMS(int recordId,
                                Externalizable externalizableObject) throws IOException, DeserializationException
    {
        try
        {
            byte[] data = this.recordStore.getRecord(recordId);
            
            //LOG
            ExtUtil.deserialize(data, externalizableObject);
        }
        catch (RecordStorageException rse)
        {
            rse.printStackTrace();
            throw new IOException(rse.getMessage());
        }
        catch (DeserializationException uee) {
        	uee.printStackTrace();
        	throw new DeserializationException(uee.getMessage());
        }

    }
    
    public void synchronizeMetaData(Externalizable prototype, MetaDataObject metaPrototype) {
    	if(this.iType == RMSUtility.RMS_TYPE_META_DATA) {
    		Hashtable set = new Hashtable();
    		
    		// The goal is to look at all of the data without actually deserializing it, so
    		// we have to handle this with the enumerations, not with fetching
    		
    		IRecordStoreEnumeration en = this.enumerateRecords();
    		String output = "";
    		output += "Record ID's: ";
    		while(en.hasNextElement()) {
    			int recordId = -1;
    			try {
					recordId = en.nextRecordId();
					set.put(new Integer(recordId), Boolean.TRUE);
					output += recordId + ", ";
				} catch (RecordStorageException e) {
					// Optimally, we would delete this record here, but it's difficult to identify
					// how we'd find the id to do so...
					e.printStackTrace();
				}
    		}
    		output += "\n";
    		output += "Meta Data ID's: "; 
    		Vector toDelete = new Vector();
    		IRecordStoreEnumeration enmd = this.enumerateMetaData();
    		while(enmd.hasNextElement()) {
    			try {
    				MetaDataObject metaInstance = (MetaDataObject)metaPrototype.getClass().newInstance();
					metaDataRMS.retrieveFromRMS(enmd.nextRecordId(), metaInstance);
					Integer RecordID = new Integer(metaInstance.getRecordId());
					output += RecordID.intValue() + ", ";
					if(!set.containsKey(RecordID)) {
						toDelete.addElement(RecordID);
					} else {
						set.remove(RecordID);
					}
				} catch (RecordStorageException e) {
					// Optimally, we would delete this record here, but it's difficult to identify
					// how we'd find the id to do so...
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DeserializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		output += "\n";
    		
    		output += "Deleting: ";
    		for(Enumeration dem = toDelete.elements() ; dem.hasMoreElements() ;) {
    			int recordToDelete = ((Integer)dem.nextElement()).intValue();
    			output += "" + recordToDelete + ", ";
    			metaDataRMS.deleteRecord(recordToDelete);
    		}
    		
    		output += "\n";
    		output += "Creating new MetaData for Records: ";
    		for(Enumeration formen = set.keys() ; formen.hasMoreElements(); ) {
    			int recordWithNoMeta = ((Integer)formen.nextElement()).intValue();
    			output += recordWithNoMeta;
    			try {
    				//Retrieve the full record so we can build the appropriate meta entry
    				Externalizable dataInstance = (Externalizable)prototype.getClass().newInstance();
					retrieveFromRMS(recordWithNoMeta, dataInstance);
					
					//Create a new instance for our meta data object.
					MetaDataObject newMeta = (MetaDataObject)metaPrototype.getClass().newInstance();
					newMeta.setMetaDataParameters(dataInstance);
					
					//Save the newly formed meta record
					metaDataRMS.writeToRMS(newMeta, null);
					output += "s";
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DeserializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			output +=  ", ";
    		}
    		
    		System.out.println(output);
    		JavaRosaServiceProvider.instance().logIncident("Debug", output);
    	}
    }

    /**
     * Retrieves a block of bytes from the RecordStore associated with the
     * given recordId 
     * @param recordId The Id of the record to be retrieved
     * @return The set of bytes associated with recordId
     * @throws IOException Thrown if the RecordStore fails to retreive any data
     */
    public byte[] retrieveByteDataFromRMS(int recordId) throws IOException
    {
        try
        {
            byte[] data = this.recordStore.getRecord(recordId);
            return data;
        }
        catch (RecordStorageException rse)
        {
            rse.printStackTrace();
            throw new IOException(rse.getMessage());
        }

    }

    /**
     * Retrieves the Meta Data associated with the given recordId from this
     * utility's Meta Data RecordStore 
     * 
     * @param recordId The id of the record whose meta data will be returned
     * @param externalizableObject The meta data associated with the given record Id
     */
    public void retrieveMetaDataFromRMS(int recordId,
                                        Externalizable externalizableObject)
    {
    	try{
    		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
    		{
    			this.metaDataRMS.retrieveFromRMS(recordId, externalizableObject);
    		}
    	}
    	catch (IOException ex)
    	{
    		ex.printStackTrace();
    	}
        catch (DeserializationException uee) {
        	uee.printStackTrace();
        }
    }

    /**
     * Gets the total number of records stored by this RMS Utility
     * 
     * @return The total number of records that can be retreived
     */
    public int getNumberOfRecords()
    {
        int numRecords = 0;
        try
        {
            numRecords = this.recordStore.getNumRecords();
        }
        catch (RecordStorageException e)
        {
            e.printStackTrace();
        }

        return numRecords;
    }

    public MetaDataObject newMetaData (Object o) {
    	if(this.iType == RMSUtility.RMS_TYPE_META_DATA) {
    		throw new RuntimeException("RMS Utility " + this.RS_NAME + " is a meta data typed RMS, but doesn't implement the newMetaData method");
    	}
    	return null;
    }
    
    /**
     * Returns an enumeration of the meta data for the objects 
     * stored in this RMS Utility.
     * 
     * @return a RecordEnumeration of the MetaData stored in this utility
     */
    public IRecordStoreEnumeration enumerateMetaData() {
    	//TODO check if need to open / close
		if (this.iType == RMSUtility.RMS_TYPE_STANDARD){
			try {
				//TODO check if this is correct return
				return this.recordStore.enumerateRecords();
			} catch (RecordStorageException e) {
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			}
			
		}else{
			return metaDataRMS.enumerateMetaData();
		}
		return null;
	}
    
    /**
     * Returns an enumeration of the objects stored in this RMS Utility.
     * 
     * @return a RecordEnumeration of the Data stored in this utility
     */
    public IRecordStoreEnumeration enumerateRecords() {
		try {
			// TODO check if this is correct return
			return this.recordStore.enumerateRecords();
		} catch (RecordStorageException e) {
			// #if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			// #endif
		}
		return null;
	}
    
    /**
     * Gets the ID of the next record that will be stored
     * in this Utility
     * 
     * @return an integer value of the id that will be associated
     * with the next object stored in this Utility
     */
    public int getNextRecordID(){
    	this.open();
    	
    	try {
			return this.recordStore.getNextRecordID();
		} catch (RecordStorageException e) {
			//#if debug.output==verbose || debug.output==exception
			// TODO Auto-generated catch block
			e.printStackTrace();
			//#endif
		}
		return -1;
    }
    
    /**
     * Empty's the set of records for this Utility
     */
	public void tempEmpty() {
		
		this.open();
		IRecordStoreEnumeration recordEnum;
		try {
			recordEnum = recordStore.enumerateRecords();
			while(recordEnum.hasNextElement())
			{
				int i = recordEnum.nextRecordId();
				this.recordStore.deleteRecord(i);		
			}
		} catch (RecordStorageException e) {
			// TODO Auto-generated catch block
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA){
			this.metaDataRMS.tempEmpty();
        }
	}
	
	public float getConsumedSpace() {
		float currentSize = 0;
    	//get total size of record store
    	try {
			currentSize = recordStore.getSize();
			if(this.metaDataRMS != null) {
				currentSize += this.metaDataRMS.getConsumedSpace();
			}
			}
    	catch (RecordStorageException e) {
			// TODO Auto-generated catch block
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
    
    	return currentSize;
	}
	
	public float getAvailableSpace() {
		float currentSize = 0;
    	//get total size of record store
    	try {
			currentSize = recordStore.getSizeAvailable();
			if(this.metaDataRMS != null) {
				currentSize += this.metaDataRMS.getAvailableSpace();
			}
			}
    	catch (RecordStorageException e) {
			// TODO Auto-generated catch block
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
    
    	return currentSize;
	}

    public float computeSpace()
    {
    	float currentSize = 0;
    	float remainingSize = 0;
    	float totalSize = 0;
    	float space = 0;
    	//get total size of record store
    	try {
			currentSize = recordStore.getSize();
			remainingSize = recordStore.getSizeAvailable();
			totalSize = currentSize + remainingSize;
			}
    	catch (RecordStorageException e) {
			// TODO Auto-generated catch block
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
    	
    	space = remainingSize/totalSize;
    
    	return space;
    }
    
    public boolean makeIDsAvailable (Vector recIDs) {
    	int maxRecID = -1;
    	for (int i = 0; i < recIDs.size(); i++) {
    		maxRecID = Math.max(maxRecID, ((Integer)recIDs.elementAt(i)).intValue());
    	}
    	
    	//allocate records up to the maximum needed id
    	try {
	    	while (maxRecID >= recordStore.getNextRecordID()) {
	    		recordStore.addRecord(null, 0, 0);
	    	}
    	} catch (RecordStorageException rse) {
    		return false;
    	}
	    	
    	//test setting each record id
    	for (int i = 0; i < recIDs.size(); i++) {
    		int recID = ((Integer)recIDs.elementAt(i)).intValue();
    		try {
    			recordStore.setRecord(recID, null, 0, 0);
    		} catch (RecordStorageException rse) {
    			return false;
    		}
    	}
    	
    	//clean up record ids that are unused
    	try {
	    	for (IRecordStoreEnumeration e = recordStore.enumerateRecords(); e.hasNextElement(); ) {
	    		int recID = e.nextRecordId();
	    		if (!recIDs.contains(new Integer(recID)) && recordStore.getRecord(recID) == null) {
	    			recordStore.deleteRecord(recID);
	    		}
	    	}
    	} catch (RecordStorageException rse) {
    		return false;
    	}
    	
    	if (metaDataRMS != null) {
    		return metaDataRMS.makeIDsAvailable(recIDs);
    	} else {
    	  	return true;
    	}
    }
    
}