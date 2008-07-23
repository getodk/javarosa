package org.javarosa.core.model.storage;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;
/**
 * The RMS persistent storage utility for FormDef
 * objects.
 *
 * @author Clayton Sims
 */
public class FormDefRMSUtility extends RMSUtility {

	/** Prototypes for IDataReferences and IAnswerData */
	private PrototypeFactory questionFactory;
	
	/** Prototypes for IDataModel */
	private PrototypeFactory modelFactory;
	
	/**
	 * Creates a new RMS utility with the given name
	 * @param name A unique identifier for this RMS utility
	 */
	public FormDefRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_META_DATA);
	}
    public void retrieveFromRMS(int recordId,
            FormDef externalizableObject) throws IOException, IllegalAccessException, InstantiationException, UnavailableExternalizerException {
    	externalizableObject.setModelFactory(this.modelFactory);
    	super.retrieveFromRMS(recordId, externalizableObject);
    }

	
	/**
	 * @return The name to be used for this RMS Utility
	 */
	public static String getUtilityName() {
		return "FormDef RMS Utility";
	}

	/**
	 * Writes the given formdefinition to RMS
	 * @param form The definition of the form to be written
	 */
	public void writeToRMS(FormDef form) {
		super.writeToRMS(form, new FormDefMetaData(form));
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
			RecordEnumeration recordEnum = recordStore.enumerateRecords(null,
					null, false);
			while (recordEnum.hasNextElement()) {
				int i = recordEnum.nextRecordId();
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
		FormDefMetaData xformMetaData = new FormDefMetaData();
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
	
	public PrototypeFactory getQuestionElementsFactory() {
		if(questionFactory == null) { 
			questionFactory = new PrototypeFactory();
			addAnswerDataPrototype(new DateData());
			addAnswerDataPrototype(new IntegerData());
			addAnswerDataPrototype(new SelectMultiData());
			addAnswerDataPrototype(new SelectOneData());
			addAnswerDataPrototype(new StringData());
		}
		return questionFactory; 
	}
	
	public void addReferencePrototype(IDataReference reference) {
		getQuestionElementsFactory().addNewPrototype(reference.getClass().getName(), reference.getClass());
	}
	
	public void addAnswerDataPrototype(IAnswerData answerData) {
		getQuestionElementsFactory().addNewPrototype(answerData.getClass().getName(), answerData.getClass());
	}
	
	public void clearQuestionElementsFactory() {
		questionFactory = null;
	}
	
	private PrototypeFactory getModelFactory() {
		if(modelFactory == null) { 
			modelFactory = new PrototypeFactory();
		}
		return modelFactory; 
	}
	
	public void addModelPrototype(IFormDataModel model) {
		this.getModelFactory().addNewPrototype(model.getClass().getName(), model.getClass());
	}
	
	public void clearModelFactory() {
		modelFactory = null;
	}
}