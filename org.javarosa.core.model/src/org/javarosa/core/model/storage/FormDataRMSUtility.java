package org.javarosa.core.model.storage;

import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.model.FormData;
import org.javarosa.core.services.storage.utilities.RMSUtility;
/**
 * The RMS Persistent storage utility for FormData objects
 *
 * @author Clayton Sims
 */
public class FormDataRMSUtility extends RMSUtility {

	/**
	 * Makes a new FormDataRMSUtility 
	 * @param name The unique name of this utility
	 */
	public FormDataRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}

	/**
	 * Gets the name to be used for this utility
	 * 
	 * @return The unique name to be used to register this utility
	 */
	public static String getUtilityName() {
		return "FormData RMS Utility";
	}
	
	/**
	 * Writes the given form data to persistent storage
	 * @param form The form to be written
	 */
	public void writeToRMS(FormData form) {
		super.writeToRMS(form, new FormDataMetaData(form));
	}

	/**
	 * Writes the block of bytes to this RMS with Meta DAta
	 * @param ba The block of bytes
	 */
	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new FormDataMetaData());
	}

	/**
	 * Returns the size of the given record in the RMS
	 * 
	 * @param recordId The id of the record whose size is to be returned
	 * @return The size, in bytes, of the record with the given index
	 */
	public int getSize(int recordId) {
		FormDataMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getSize();
	}

	/**
	 * Gets the name of given record in the RMS
	 * 
	 * @param recordId The id of the record whose name is to be returned
	 * @return The name of the record with the given index
	 */
	public String getName(int recordId) {
		FormDataMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getName();
	}

	/**
	 * Gets the meta data object for the record with the given Id
	 * 
	 * @param recordId The id of the record whose meta data is to be returned
	 * @return The meta data of the record with the given Id
	 */
	private FormDataMetaData getMetaDataFromId(int recordId) {
		FormDataMetaData formMetaData = new FormDataMetaData();
		this.retrieveMetaDataFromRMS(recordId, formMetaData);
		return formMetaData;
	}

	/**
	 * @return a list of form names that are stored in this RMS
	 */
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

	/**
	 * Gets a list of form names that correspond to the records present in this RMS
	 * which have the ids given.
	 * 
	 * @param formIDs A vector of formIds
	 * @return A vector of strings which are the names of each record in this RMS
	 * with an id that exists in the provided vector of ids.
	 */
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

	/**
	 * Retrieves an id number corresponding to a form in this RMS which has the 
	 * name that is given as an argument
	 * 
	 * @param name The name of a form to be returned from the RMS
	 * @return an id for a form in this RMS which shares the name given, if one
	 * exists. -1 otherwise.
	 */
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

	/**
	 * @return a list of MetaData for the form data objects stored in this RMS
	 */
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