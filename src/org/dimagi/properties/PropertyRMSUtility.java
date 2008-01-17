package org.dimagi.properties;

import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.celllife.clforms.storage.Externalizable;
import org.celllife.clforms.storage.RMSUtility;
import org.celllife.clforms.util.SimpleOrderedHashtable;

public class PropertyRMSUtility extends RMSUtility {

    SimpleOrderedHashtable nameToId = new SimpleOrderedHashtable();
    
    public PropertyRMSUtility(String name)
    {
        super(name, RMSUtility.RMS_TYPE_META_DATA);
        System.out.println("PropertyRMSUtility()");
    }
    
    public void writeToRMS(Property property)
    {
        super.writeToRMS(property, new PropertyMetaData(property));
    }

    public int getSize(int recordId)
    {
        PropertyMetaData propertyMetaData = new PropertyMetaData();
        this.retrieveMetaDataFromRMS(recordId, propertyMetaData);
        return propertyMetaData.getSize();
    }

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
    
    public String getValue(String recordName ) {
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
    
    public void writeValue(String propertyName, String value) {
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
