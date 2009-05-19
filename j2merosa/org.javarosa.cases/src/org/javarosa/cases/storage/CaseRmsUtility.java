/**
 * 
 */
package org.javarosa.cases.storage;

import java.util.Vector;

import org.javarosa.cases.model.Case;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseRmsUtility extends RMSUtility {

	public CaseRmsUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}
	
	public CaseRmsUtility(String name, int type) {
		super(name, type);
	}
	
	/**
	 * Gets the name to be used for this utility
	 * 
	 * @return The unique name to be used to register this utility
	 */
	public static String getUtilityName() {
		return "CASE_RMS";
	}
	
	/**
	 * Writes the given model data to persistent storage
	 * @param model The model to be written
	 * @return new record ID
	 */
	public int writeToRMS(Case c) {
		return super.writeToRMS(c, new CaseMetaDataObject(c));
	}

	public void updateToRMS(int recordId, Case c) {
		updateToRMS(recordId, c, getMetaDataFromId(recordId));
	}
	
	/**
	 * Writes the block of bytes to this RMS with Meta DAta
	 * @param ba The block of bytes
	 */
	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new CaseMetaDataObject());
	}

	/**
	 * Returns the size of the given record in the RMS
	 * 
	 * @param recordId The id of the record whose size is to be returned
	 * @return The size, in bytes, of the record with the given index
	 */
	public int getSize(int recordId) {
		CaseMetaDataObject patientMetaData = getMetaDataFromId(recordId);
		return patientMetaData.getSize();
	}

	/**
	 * Gets the name of given record in the RMS
	 * 
	 * @param recordId The id of the record whose name is to be returned
	 * @return The name of the record with the given index
	 */
	public String getName(int recordId) {
		CaseMetaDataObject caseMetaData = getMetaDataFromId(recordId);
		return caseMetaData.getCaseName();
	}

	/**
	 * Gets the meta data object for the record with the given Id
	 * 
	 * @param recordId The id of the record whose meta data is to be returned
	 * @return The meta data of the record with the given Id
	 */
	private CaseMetaDataObject getMetaDataFromId(int recordId) {
		CaseMetaDataObject patientMetaData = new CaseMetaDataObject();
		this.retrieveMetaDataFromRMS(recordId, patientMetaData);
		return patientMetaData;
	}
	
	/**
	 * @return a list of form names that are stored in this RMS
	 */
	public Vector getListOfFormNames() {
		Vector listOfNames = new Vector();
		try {
			IRecordStoreEnumeration recordEnum = this.getRecordStore().enumerateRecords();
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
				listOfNames.addElement(this.getName(i));

			}
		} catch (RecordStorageException e) {
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
			IRecordStoreEnumeration recordEnum = this.getRecordStore().enumerateRecords();
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
				listOfNames.addElement(this.getName(i));
				formIDs.addElement(new Integer(i));
			}
		} catch (RecordStorageException e) {
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
		CaseMetaDataObject caseMetaData = new CaseMetaDataObject();
		try {
			IRecordStoreEnumeration recEnum = this.getRecordStore().enumerateRecords();
			while (recEnum.hasNextElement()) {
				id = recEnum.nextRecordId();
				this.retrieveMetaDataFromRMS(id, caseMetaData);
				if (caseMetaData.getCaseName().equals(name)) {
					break;
				}
				id = -1;
			}
		} catch (RecordStorageException ex) {
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
			IRecordStoreEnumeration metaEnum = metaDataRMS.enumerateMetaData();
			while (metaEnum.hasNextElement()) {
				int i = metaEnum.nextRecordId();
				metaDataList.addElement(getMetaDataFromId(i));
			}
		} catch (RecordStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metaDataList;
	}
	
	public MetaDataObject newMetaData (Object o) {
    	CaseMetaDataObject c = new CaseMetaDataObject((Case)o);
    	return c;
    }
}
