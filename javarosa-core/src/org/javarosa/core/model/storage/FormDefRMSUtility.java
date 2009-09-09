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

package org.javarosa.core.model.storage;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;
/**
 * The RMS persistent storage utility for FormDef
 * objects.
 * 
 * This class contains prototype factories for 
 * serializing and deserializing various elements of
 * the core JavaRosa model infrastructure, such as
 * IDataReferences, IAnswerData's, and IDataModelTrees.
 *
 * @author Clayton Sims
 */
public class FormDefRMSUtility extends RMSUtility {	
	/**
	 * Creates a new RMS utility with the given name
	 * @param name A unique identifier for this RMS utility
	 */
	public FormDefRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}
	
    public void retrieveFromRMS(int recordId, FormDef externalizableObject) throws IOException, DeserializationException {
    	super.retrieveFromRMS(recordId, externalizableObject);
    }

	
	/**
	 * @return The name to be used for this RMS Utility
	 */
	public static String getUtilityName() {
		return "FORM_DEF_RMS";
	}

	/**
	 * Writes the given formdefinition to RMS
	 * @param form The definition of the form to be written
	 * @return new record ID
	 */
	public int writeToRMS(FormDef form) {
		return super.writeToRMS(form, new FormDefMetaData(form));
	}

	public void updateToRMS(int recordId, FormDef form) {
		updateToRMS(recordId, form, getMetaDataFromId(recordId));
	}
	
	public MetaDataObject newMetaData (Object o) {
		return new FormDefMetaData((FormDef)o);
	}
	
	/**
	 * Writes the given block of bytes to RMS
	 * @param ba The set of bytes to be written
	 */
	public void writeToRMS(byte[] ba) {
		super.writeBytesToRMS(ba, new FormDefMetaData());
	}

	/**
	 * Returns the size of the given record in the RMS
	 * 
	 * @param recordId The id of the record whose size is to be returned
	 * @return The size, in bytes, of the record with the given index
	 */
	public int getSize(int recordId) {
		FormDefMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getSize();
	}

	/**
	 * Gets the name of given record in the RMS
	 * 
	 * @param recordId The id of the record whose name is to be returned
	 * @return The name of the record with the given index
	 */
	public String getName(int recordId) {
		FormDefMetaData xformMetaData = getMetaDataFromId(recordId);
		return xformMetaData.getName();
	}

	/**
	 * Gets the meta data object for the record with the given Id
	 * 
	 * @param recordId The id of the record whose meta data is to be returned
	 * @return The meta data of the record with the given Id
	 */
	private FormDefMetaData getMetaDataFromId(int recordId) {
		FormDefMetaData formMetaData = new FormDefMetaData();
		this.retrieveMetaDataFromRMS(recordId, formMetaData);
		return formMetaData;
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

	public int getIDfromName(String name) {
		// TODO Check if this is still needed / valid - considering two forms
		// can have same name
		int id = -1;
		this.open();
		FormDefMetaData xformMetaData = new FormDefMetaData();
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
}