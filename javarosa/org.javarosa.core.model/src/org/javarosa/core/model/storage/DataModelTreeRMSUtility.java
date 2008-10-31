package org.javarosa.core.model.storage;

import java.util.Vector;

import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;

/**
 * The RMS persistent storage utility for DataModelTree
 * objects.
 *
 * @author Clayton Sims
 */
public class DataModelTreeRMSUtility extends RMSUtility {

	/**
	 * Makes a new DataModelTreeRMSUtility 
	 * @param name The unique name of this utility
	 */
	public DataModelTreeRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}

	/**
	 * Gets the name to be used for this utility
	 * 
	 * @return The unique name to be used to register this utility
	 */
	public static String getUtilityName() {
		return "DATA_MODEL_TREE_RMS";
	}
	
	/**
	 * Writes the given model data to persistent storage
	 * @param model The model to be written
	 * @return new record ID
	 */
	public int writeToRMS(DataModelTree model) {
		return super.writeToRMS(model, new DataModelTreeMetaData(model));
	}

	public void updateToRMS(int recordId, DataModelTree model) {
		updateToRMS(recordId, model, getMetaDataFromId(recordId));
	}
	
	/**
	 * Writes the block of bytes to this RMS with Meta DAta
	 * @param ba The block of bytes
	 */
	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new DataModelTreeMetaData());
	}

	/**
	 * Returns the size of the given record in the RMS
	 * 
	 * @param recordId The id of the record whose size is to be returned
	 * @return The size, in bytes, of the record with the given index
	 */
	public int getSize(int recordId) {
		DataModelTreeMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getSize();
	}

	/**
	 * Gets the name of given record in the RMS
	 * 
	 * @param recordId The id of the record whose name is to be returned
	 * @return The name of the record with the given index
	 */
	public String getName(int recordId) {
		DataModelTreeMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getName();
	}

	/**
	 * Gets the meta data object for the record with the given Id
	 * 
	 * @param recordId The id of the record whose meta data is to be returned
	 * @return The meta data of the record with the given Id
	 */
	private DataModelTreeMetaData getMetaDataFromId(int recordId) {
		DataModelTreeMetaData formMetaData = new DataModelTreeMetaData();
		this.retrieveMetaDataFromRMS(recordId, formMetaData);
		return formMetaData;
	}

	/**
	 * @return a list of model names that are stored in this RMS
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
	 * Gets a list of model names that correspond to the records present in this RMS
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
	 * Retrieves an id number corresponding to a model in this RMS which has the 
	 * name that is given as an argument
	 * 
	 * @param name The name of a model to be returned from the RMS
	 * @return an id for a model in this RMS which shares the name given, if one
	 * exists. -1 otherwise.
	 */
	public int getIDfromName(String name) {
		// TODO Check if this is still needed / valid - considering two forms
		// can have same name
		int id = -1;
		this.open();
		DataModelTreeMetaData xformMetaData = new DataModelTreeMetaData();
		try {
			IRecordStoreEnumeration recEnum = this.getRecordStore().enumerateRecords();
			while (recEnum.hasNextElement()) {
				id = recEnum.nextRecordId();
				this.retrieveMetaDataFromRMS(id, xformMetaData);
				if (xformMetaData.getName().equals(name)) {
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
	 * @return a list of MetaData for the model data objects stored in this RMS
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
}
