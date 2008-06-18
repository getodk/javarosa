package org.javarosa.properties;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.clforms.storage.RMSUtility;
import org.javarosa.clforms.util.SimpleOrderedHashtable;

/**
 * The PropertyRMSUtility interacts with the RMS storage on the local device to read
 * and write Property records.
 * 
 * @author ctsims
 *
 */
public class PropertyRMSUtility extends RMSUtility {

    SimpleOrderedHashtable nameToId = new SimpleOrderedHashtable();
    
    /**
     * Creates a propertyRMSUtility with the given name.
     * @param name The name of this PropertyRMSUtility. This will be used to specify which
     * utility will be returned from the RMS Utility Manager
     */
    public PropertyRMSUtility(String name)
    {
        super(name, RMSUtility.RMS_TYPE_META_DATA);
        System.out.println("PropertyRMSUtility()");
    }
    
    /**
     * Writes a given property to the RMS
     * @param property The property to be written
     */
    public void writeToRMS(Property property)
    {
        super.writeToRMS(property, new PropertyMetaData(property));
    }

    /**
     * Gets the size of the record given by recordId
     * @param recordId The record that will be checked
     * @return The size of the specified record
     */
    public int getSize(int recordId)
    {
        PropertyMetaData propertyMetaData = new PropertyMetaData();
        this.retrieveMetaDataFromRMS(recordId, propertyMetaData);
        return propertyMetaData.getSize();
    }

    /**
     * Gets the name of the property in the record specified by the given ID
     * 
     * @param recordId The id of the record to be retrieved
     * @return The name of the property of the record specified
     */
    public String getName(int recordId)
    {
        PropertyMetaData propertyMetaData = new PropertyMetaData();
        this.retrieveMetaDataFromRMS(recordId, propertyMetaData);
        return propertyMetaData.getName();
    }
    private void indexPropertyNames() {
        try {
            RecordEnumeration recordEnum = recordStore.enumerateRecords(null,null,false);
            while(recordEnum.hasNextElement())
            {
                int i = recordEnum.nextRecordId();
                System.out.println("trying record:"+i);
                nameToId.put(this.getName(i),new Integer(i));                
            }
        } catch (RecordStoreNotOpenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RecordStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the value of a given property 
     * @param recordName The property to be retrieved
     * @return The value of the property if it exists, null otherwise
     */
    public Vector getValue(String recordName ) {
        Object recordId = nameToId.get(recordName);
        if(recordId == null) {
            indexPropertyNames();
            recordId = nameToId.get(recordName);
        }
        if(recordId != null) {
            Property readProperty = new Property();
            try {
            this.retrieveFromRMS(((Integer)recordId).intValue(), readProperty);
            return readProperty.value;
            }
            catch( IOException e ){
             // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
        else {
            return null;
        }
    }
    
    /**
     * Writes a string value to a given property. Creates the property record if none exists
     *  
     * @param propertyName The name of the property to be written
     * @param value The value of the property
     */
    public void writeValue(String propertyName, Vector value) {
        Property theProp = new Property();
        theProp.name = propertyName;
        theProp.value = value;
        
        indexPropertyNames();
        Object recordId = nameToId.get(propertyName);
        if(recordId == null) {
            this.writeToRMS(theProp);            
        }else {
            PropertyMetaData pmd = new PropertyMetaData(theProp);
            this.updateToRMS(((Integer)recordId).intValue(), theProp, pmd);
        }
    }
}
