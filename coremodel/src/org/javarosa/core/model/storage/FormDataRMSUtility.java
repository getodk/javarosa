package org.javarosa.core.model.storage;

import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.model.FormData;
import org.javarosa.core.services.storage.utilities.RMSUtility;
/**
 *
 * @author Clayton Sims
 */
public class FormDataRMSUtility extends RMSUtility {

	public FormDataRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}

	public static String getUtilityName() {
		return "FormData RMS Utility";
	}
	
	public void writeToRMS(FormData form) {
		super.writeToRMS(form, new FormDataMetaData(form));
	}

	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new FormDataMetaData());
	}

	public int getSize(int recordId) {
		FormDataMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getSize();
	}

	public String getName(int recordId) {
		FormDataMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getName();
	}

	private FormDataMetaData getMetaDataFromId(int recordId) {
		FormDataMetaData formMetaData = new FormDataMetaData();
		this.retrieveMetaDataFromRMS(recordId, formMetaData);
		return formMetaData;
	}

	public Vector getListOfFormNames() {
		Vector listOfNames = new Vector();
		try {
			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
				System.out.println("trying record:" + i);
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

	public Vector getListOfFormNames(Vector formIDs) {
		Vector listOfNames = new Vector();
		try {
			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
				System.out.println("trying record:" + i);
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

	public int getIDfromName(String name) {
		// TODO Check if this is still needed / valid - considering two forms
		// can have same name
		int id = -1;
		this.open();
		FormDataMetaData xformMetaData = new FormDataMetaData();
		try {
			RecordEnumeration recEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recEnum.hasNextElement()) {
				id = recEnum.nextRecordId();
				this.retrieveMetaDataFromRMS(id, xformMetaData);
				if (xformMetaData.getName().equals(name)) {
					break;
				}
				id = -1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return id;
	}

	public Vector getFormMetaDataList() {
		Vector metaDataList = new Vector();
		try {
			RecordEnumeration metaEnum = metaDataRMS.enumerateMetaData();
			while (metaEnum.hasNextElement()) {
				int i = metaEnum.nextRecordId();
				metaDataList.addElement(getMetaDataFromId(i));
			}
		} catch (InvalidRecordIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metaDataList;
	}

}