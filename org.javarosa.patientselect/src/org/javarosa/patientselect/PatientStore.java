package org.javarosa.patientselect;

import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.Serializer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;

public class PatientStore  implements RecordListener {

	private String recordStoreName = "";
	private int iType = RMSUtility.RMS_TYPE_STANDARD;
	
	protected RMSUtility metaDataRMS;
    protected RecordStore recordStore = null;
	private String RS_NAME = "recordStore";
	
	public PatientStore(String name, int type) {
		
		this.recordStoreName = name;
		this.iType = type;
		
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
        
        System.out.println("RMS SIZE (" + this.recordStoreName + ") : " + this.getNumberOfRecords());
	}
	
	/*
	 * Returns the name [Type: String] of the current open RecordStore
	 */
	public String getName()
    {
        return this.RS_NAME;
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
                this.recordStore = RecordStore.openRecordStore(RS_NAME, true);
                this.recordStore.addRecordListener(this);
            }
            catch (RecordStoreException rse)
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
                this.recordStore.removeRecordListener(this);
                this.recordStore.closeRecordStore();
                
                if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        		{
        			this.metaDataRMS.close();
        		}
            }
            catch (RecordStoreException rse)
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
    public int writeToRMS(Object obj,MetaDataObject metaDataObject)
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
        catch (RecordStoreException rse)
        {
            rse.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return recordId;
    }
    
    /**
     * Updates the given record in the RMS, along with its metadata
     * object.
     * 
     * @param recordId The record ID for the given object
     * @param obj The Externalizable object associated wtih recordId
     * @param metaDataObject The meta data descriptor for the object
     */
    public void updateToRMS(int recordId, Object obj,MetaDataObject metaDataObject)
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
    	catch (RecordStoreException rse)
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
    	try
    	{
    		int recordId = this.recordStore.getNextRecordID();
    		
    		if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
    		{
    			metaDataObject.setRecordId(recordId);
    			metaDataObject.setSize(data.length);
    			this.metaDataRMS.writeToRMS(metaDataObject, null);
    		}
    		this.recordStore.addRecord(data, 0, data.length);
    	}
    	catch (RecordStoreException rse)
    	{
    		rse.printStackTrace();
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
        catch (InvalidRecordIDException ex)
        {
            ex.printStackTrace();
        }
        catch (RecordStoreNotOpenException ex)
        {
            ex.printStackTrace();
        }
        catch (RecordStoreException ex)
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
        	RecordStore scoresRecordStore1 = RecordStore.openRecordStore(this.RS_NAME,true);
        	scoresRecordStore1.closeRecordStore();
        	RecordStore.deleteRecordStore(this.RS_NAME);
        	
        }
        catch (Exception e)
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
    public void retrieveFromRMS(int recordId, Externalizable externalizableObject) throws IOException, DeserializationException
    {
        try
        {
            byte[] data = this.recordStore.getRecord(recordId);
            //LOG
            ExtUtil.deserialize(data, externalizableObject);
        }
        catch (RecordStoreException rse)
        {
            rse.printStackTrace();
            throw new IOException(rse.getMessage());
        }
        catch (DeserializationException uee) {
        	uee.printStackTrace();
        	throw new DeserializationException(uee.getMessage());
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
        catch (RecordStoreException rse)
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
    public void retrieveMetaDataFromRMS(int recordId,Externalizable externalizableObject)
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
        catch (IllegalAccessException iae)
        {
            iae.printStackTrace();
        }
        catch (InstantiationException ie)
        {
        	ie.printStackTrace();
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
        catch (RecordStoreNotOpenException e)
        {
            e.printStackTrace();
        }

        return numRecords;
    }

    /*
     * (non-Javadoc)
     * @see javax.microedition.rms.RecordListener#recordAdded(javax.microedition.rms.RecordStore, int)
     */
    public void recordAdded(RecordStore recordStore, int i)
    {
    }

    /*
     * (non-Javadoc)
     * @see javax.microedition.rms.RecordListener#recordChanged(javax.microedition.rms.RecordStore, int)
     */
    public void recordChanged(RecordStore recordStore, int i)
    {
    }

    /*
     * (non-Javadoc)
     * @see javax.microedition.rms.RecordListener#recordDeleted(javax.microedition.rms.RecordStore, int)
     */
    public void recordDeleted(RecordStore recordStore, int i)
    {
    }

    /**
     * Returns an enumeration of the meta data for the objects 
     * stored in this RMS Utility.
     * 
     * @return a RecordEnumeration of the MetaData stored in this utility
     */
    public RecordEnumeration enumerateMetaData() {
    	//TODO check if need to open / close
		if (this.iType == RMSUtility.RMS_TYPE_STANDARD){
			
			try {
				//TODO check if this is correct return
				return this.recordStore.enumerateRecords(null,null,false);
				
			} catch (RecordStoreNotOpenException e) {
				
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
				
			} 
			
		}
		else{
			
			return metaDataRMS.enumerateMetaData();
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
		} catch (RecordStoreNotOpenException e) {
			
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		} catch (RecordStoreException e) {
		
			//#if debug.output==verbose || debug.output==exception
			
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
		RecordEnumeration recordEnum;
		try {
			recordEnum = recordStore.enumerateRecords(null,null,false);
			while(recordEnum.hasNextElement())
			{
				int i = recordEnum.nextRecordId();
				this.recordStore.deleteRecord(i);		
			}
		} catch (RecordStoreNotOpenException e) {
			// TODO Auto-generated catch block
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		} catch (InvalidRecordIDException e) {
			//#if debug.output==verbose || debug.output==exception
			// TODO Auto-generated catch block
			e.printStackTrace();
			//#endif
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			//#endif
		}
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA){
			this.metaDataRMS.tempEmpty();
        }
	
	}

}
