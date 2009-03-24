package org.javarosa.core.model.storage;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.storage.utilities.IRecordStorage;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.services.transport.TransportMessage;

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
	
	private double getUsageFactor () {
		IRecordStorage rs = getRecordStore();
		if (rs == null) {
			throw new RuntimeException("DataModelTreeRMSUtility.getUsageFactor: RMS not open");
		}
		
		double factor;
		try {
			factor = rs.getSize() / (double)(rs.getSize() + rs.getSizeAvailable());
		} catch (RecordStorageException e) {
			factor = Double.NaN;
		}		
		
		return factor;
	}
	
	/**
	 * Clean out the RMS when it's getting too full. Only deletes saved forms that have not
	 * been successfully sent.
	 * 
	 * @param triggerThreshold how full the RMS must be to commence purging (.8 == 80% full)
	 * @param clearThreshold how full it should be before we stop purging (0. == purge every available record)
	 * @param ageThreshold how old a record must be before it may be purged (in days)
	 * @return whether the RMS fullness is less than the trigger threshold when done purging. if it's not
	 *    we're in serious trouble
	 */
	public boolean purge (double triggerThreshold, double clearThreshold, double ageThreshold) {
		this.open();

		boolean ok;

		System.out.println("Saved Form Purge: " + (100. * getUsageFactor()) + " pct consumed");
		
		if (getUsageFactor() >= triggerThreshold) {
			System.out.println("Saved Form Purge: purging...");
			
			Vector deletables = new Vector();

			//collect all saved models older than the threshold
	    	Date now = new Date();
			IRecordStoreEnumeration instances = metaDataRMS.enumerateMetaData();
			while (instances.hasNextElement()) {
				DataModelTreeMetaData meta = null;
				try {
					meta = getMetaDataFromId(instances.nextRecordId());
				} catch (RecordStorageException e) {
					e.printStackTrace();
				}
				
				if ((now.getTime() - meta.getDateSaved().getTime()) > ageThreshold * 86400000l)
					deletables.addElement(new Integer(meta.getRecordId()));
			}

			//should really sort by date here so that the oldest get deleted first, but god, what a p.i.t.a.
			
			//eliminate all models not yet sent (we do this in a separate pass because its more efficient to
			//get the send-status information in bulk
			ITransportManager tm = JavaRosaServiceProvider.instance().getTransportManager();
			Vector sendStatuses = tm.getModelDeliveryStatuses(deletables, true); //notFoundOK == true because i assume
			   //if a form has never been attempted to send, it won't have a corresponding entry in the message queue
			   //this could be a problem for sent forms whose message queue entry has disappeared... these will be
			   //treated as unsent (thus undeletable) forever
			for (int i = 0, j = 0; i < deletables.size(); i++, j++) {
				if (((Integer)sendStatuses.elementAt(j)).intValue() != TransportMessage.STATUS_DELIVERED) {
					deletables.removeElementAt(i);
					i--;
				}
			}
			
			System.out.println("Saved Form Purge: " + deletables.size() + " purgable records");
			
			boolean emptyEnough = false;
			int numPurged = 0;
			for (int i = 0; i < deletables.size() && !emptyEnough; i++) {
				int modelID = ((Integer)deletables.elementAt(i)).intValue();
				deleteRecord(modelID);
				numPurged++;
				
				if (getUsageFactor() <= clearThreshold)
					emptyEnough = true;
			}
			
			System.out.println("Saved Form Purge: " + numPurged + " records purged; " +
					(100. * getUsageFactor()) + " pct consumed");
			
			ok = getUsageFactor() < triggerThreshold;
		} else {
			ok = true;
		}
    	
		//this.close();
		return ok;
	}
}
