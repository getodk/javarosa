/*
 * RMSUtility.java
 *
 * Created on September 10, 2007, 5:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Munier
 */
package org.celllife.clforms.storage;

import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;



public class RMSUtility implements RecordListener
{

    public static final int RMS_TYPE_STANDARD = 0;
    public static final int RMS_TYPE_META_DATA = 1;
    /** Creates a new instance of RMSUtility */
    private String RS_NAME = "";
    private int iType = RMSUtility.RMS_TYPE_STANDARD;
    protected RMSUtility metaDataRMS;
    protected RecordStore recordStore = null;

    public RMSUtility(String name, int iType)
    {
//    	System.out.println("RMSUtility.RMSUtility()");
        this.RS_NAME = name;
        this.iType = iType;
        if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        {
            this.metaDataRMS = new RMSUtility("META_DATA_" + name, RMSUtility.RMS_TYPE_STANDARD);
        }
        
        this.open();
        System.out.println("RMS SIZE (" + this.RS_NAME + ") : " + this.getNumberOfRecords());
    }

    public String getName()
    {
//    	System.out.println("RMSUtility.getName()");
    	return this.RS_NAME;
    }

    public void open()
    {
//    	System.out.println("RMSUtility.open()");
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

    public void close()
    {
//    	System.out.println("RMSUtility.close()");
        if (this.recordStore != null)
        {
            try
            {
                this.recordStore.removeRecordListener(this);
                this.recordStore.closeRecordStore();
                System.out.println("closed:"+this.recordStore.getName());
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

    public void writeToRMS(Object obj,
                           MetaDataObject metaDataObject)
    {
//    	System.out.println("******RMSUtility.writeToRMS()");
        try
        {
            int recordId = this.recordStore.getNextRecordID();
            IDRecordable recordableObject = (IDRecordable) obj;
            recordableObject.setRecordId(recordId);
            Externalizable externalizableObject = (Externalizable) obj;
            byte[] data = Serializer.serialize(externalizableObject);
            //LOG
            System.out.println("writing:"+new String(data)+"\n*** to "+recordId);
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
    }
    
    public void updateToRMS(int recordId, Object obj,
    		MetaDataObject metaDataObject)
    {
//    	System.out.println("RMSUtility.updateToRMS()");
    	try
    	{
    		System.out.println("UPDATE RMS @ "+recordId);
    		IDRecordable recordableObject = (IDRecordable) obj;
    		recordableObject.setRecordId(recordId);
    		Externalizable externalizableObject = (Externalizable) obj;
    		byte[] data = Serializer.serialize(externalizableObject);
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
    
    
    public void writeBytesToRMS(byte [] data, MetaDataObject metaDataObject)
    {
//    	System.out.println("***************writeBytesToRMS()");
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

    public void deleteRecord(int recordId)
    {
//    	System.out.println("RMSUtility.deleteRecord()");
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

    public void delete()
    {
//    	System.out.println("RMSUtility.delete()");
        try
        {   
        	System.out.println("in delete:"+this.RS_NAME);
        	
        	if (this.iType == RMSUtility.RMS_TYPE_META_DATA)
        	{
        		this.metaDataRMS.delete();
        	}
        	System.out.println("try delete:"+this.RS_NAME);
        	RecordStore scoresRecordStore1 = RecordStore.openRecordStore(this.RS_NAME,true);
        	scoresRecordStore1.closeRecordStore();
        	RecordStore.deleteRecordStore(this.RS_NAME);
        	System.out.println("try delete end:"+this.RS_NAME);
            //this.recordStore.deleteRecordStore(this.RS_NAME);
        	
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void retrieveFromRMS(int recordId,
                                Externalizable externalizableObject) throws IOException
    {
//    	System.out.println("RMSUtility.retrieveFromRMS()");
        try
        {
            byte[] data = this.recordStore.getRecord(recordId);
            //LOG
            //System.out.println("retreived data"+new String(data));
            Serializer.deserialize(data, externalizableObject);
        }
        catch (RecordStoreException rse)
        {
            rse.printStackTrace();
            throw new IOException(rse.getMessage());
        }

    }

    

    public void retrieveFromDummyForm(int recordId,
            Externalizable externalizableObject) throws IOException
    {
//    		System.out.println("RMSUtility.retrieveFromDummyForm()");
            byte[] data = DummyForm.getData();
            Serializer.deserialize(data, externalizableObject);
    }
    

    public byte[] retrieveByteDataFromRMS(int recordId) throws IOException
    {
//    	System.out.println("RMSUtility.retrieveByteDataFromRMS()");
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

    
    public void retrieveMetaDataFromRMS(int recordId,
                                        Externalizable externalizableObject)
    {
//    	System.out.println("RMSUtility.retrieveMetaDataFromRMS()");
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
    }
    
    public int getNumberOfRecords()
    {
//    	System.out.println("RMSUtility.getNumberOfRecords()");
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

    public void recordAdded(RecordStore recordStore, int i)
    {
//    	System.out.println("RMSUtility.recordAdded()");
    }

    public void recordChanged(RecordStore recordStore, int i)
    {
//    	System.out.println("RMSUtility.recordChanged()");
    }

    public void recordDeleted(RecordStore recordStore, int i)
    {
//    	System.out.println("RMSUtility.recordDeleted()");
    }

    public RecordEnumeration enumerateMetaData() {
//    	System.out.println("RMSUtility.enumerateMetaData()");
    	//TODO check if need to open / close
		if (this.iType == this.RMS_TYPE_STANDARD){
			System.out.println("getting list from metaData RMS");
			try {
				//TODO check if this is correct return
				return this.recordStore.enumerateRecords(null,null,false);
			} catch (RecordStoreNotOpenException e) {
				e.printStackTrace();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			}
			
		}else{
			System.out.println("getting from mdRMS....");
			return metaDataRMS.enumerateMetaData();
		}
		return null;
	}
    
    public int getNextRecordID(){
//    	System.out.println("RMSUtility.getNextRecordID()");
    	this.open();
    	
    	try {
			return this.recordStore.getNextRecordID();
		} catch (RecordStoreNotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
    }
    
	public void tempEmpty() {
//    	System.out.println("RMSUtility.tempEmpty()");
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
			e.printStackTrace();
		} catch (InvalidRecordIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.iType == RMSUtility.RMS_TYPE_META_DATA){
			this.metaDataRMS.tempEmpty();
        }
	}

    
    
}