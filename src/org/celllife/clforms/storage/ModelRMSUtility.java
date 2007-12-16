package org.celllife.clforms.storage;

import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 *
 * @author Munier
 */
public class ModelRMSUtility extends RMSUtility
{

    public ModelRMSUtility(String name)
    {
        super(name, RMSUtility.RMS_TYPE_META_DATA);
    }

    public void writeToRMS(Model model)
    {
        super.writeToRMS(model, new ModelMetaData(model));
    }

    public int getSize(int recordId)
    {
        ModelMetaData modelMetaData = new ModelMetaData();
        this.retrieveMetaDataFromRMS(recordId, modelMetaData);
        return modelMetaData.getSize();
    }

    public String getName(int recordId)
    {
    	ModelMetaData modelMetaData = new ModelMetaData();
        this.retrieveMetaDataFromRMS(recordId, modelMetaData);
        return modelMetaData.getName();
    }

    public Vector getListOfFormNames()
    {
        Vector listOfNames = new Vector();
        try {
        	RecordEnumeration recordEnum = recordStore.enumerateRecords(null,null,false);
        	while(recordEnum.hasNextElement())
        	{
        		int i = recordEnum.nextRecordId();
				System.out.println("trying record:"+i);
				listOfNames.addElement(this.getName(i));
				
			}
		} catch (RecordStoreNotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return listOfNames;
    }
    
    public Vector getListOfFormNames(Vector formIDs)
    {
        Vector listOfNames = new Vector();
        try {
        	RecordEnumeration recordEnum = recordStore.enumerateRecords(null,null,false);
        	while(recordEnum.hasNextElement())
        	{
        		int i = recordEnum.nextRecordId();
				System.out.println("trying record:"+i);
				listOfNames.addElement(this.getName(i));
				formIDs.addElement(new Integer(i));
			}
		} catch (RecordStoreNotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return listOfNames;
    }


}