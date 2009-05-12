package org.javarosa.j2me.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.ExtUtil;

public class DumpRMS {
	public static final String DUMP_PATH_PREFIX_DEFAULT = "E:/rmsdump";
	public static final String RESTORE_FILE_PATH_DEFAULT = "E:/rmsrestore";
	
	//pathPrefix should omit leading slash; dump file name will be prefix appended with timestamp
	public static void dumpRMS (String pathPrefix) {
		if (pathPrefix == null)
			pathPrefix = DUMP_PATH_PREFIX_DEFAULT;
		String filepath = dumpFilePath(pathPrefix);
		
		try {
			FileConnection fc = (FileConnection)Connector.open("file:///" + filepath);
			if (fc.exists()) {
				System.err.println("Error: File " + filepath + " already exists");
				throw new RuntimeException("dump file already exists");
			}
			
			fc.create();
			DataOutputStream out = fc.openDataOutputStream();
			
			dumpRMS(out);
		
			fc.close();
		} catch (IOException ioe) {
			throw new RuntimeException("ioexception: " + ioe.getMessage());
		}
	}
	
	private static String dumpFilePath (String prefix) {
		String suffix = DateUtils.formatDateTime(new Date(), DateUtils.FORMAT_TIMESTAMP_SUFFIX);
		return prefix + "." + suffix;
	}
	
	public static void dumpRMS (DataOutputStream out) {
		try {
			String[] rmses = RecordStore.listRecordStores();
			if (rmses == null) //seriously??
				rmses = new String[0];
			
			ExtUtil.writeNumeric(out, rmses.length);
			
			for (int i = 0; i < rmses.length; i++) {				
				String rmsName = rmses[i];
				ExtUtil.writeString(out, rmsName);
				
				RecordStore rs = RecordStore.openRecordStore(rmsName, false);
				int numRecords = rs.getNumRecords();
				ExtUtil.writeNumeric(out, numRecords);
				
				Vector recordIDs = new Vector();
				int count = 0;
				for (RecordEnumeration re = rs.enumerateRecords(null, null, false); re.hasNextElement(); ) {
					int recID = re.nextRecordId();
					recordIDs.addElement(new Integer(recID));
					ExtUtil.writeNumeric(out, recID);

					count++;
				}
				if (count != numRecords) {
					System.err.println("Error: number of records in RMS did not match reported value");
					throw new RuntimeException("inconsistent number of records in RMS");
				}
				
				for (int j = 0; j < recordIDs.size(); j++) {
					int recID = ((Integer)recordIDs.elementAt(j)).intValue();
					byte[] data = rs.getRecord(recID);
					
					ExtUtil.writeNumeric(out, data.length);
					out.write(data);
				}

				rs.closeRecordStore();
			}
		} catch (IOException ioe) {
			throw new RuntimeException("ioexception: " + ioe.getMessage());
		} catch (RecordStoreException rse) {
			throw new RuntimeException("recordstoreexception: " + rse.getMessage());
		} finally {
			try {
				out.flush();
			} catch (IOException ioe) {	}
		}
	}	
	
	//path should omit leading slash
	public static void restoreRMS (String filepath) {
		if (filepath == null)
			filepath = RESTORE_FILE_PATH_DEFAULT;
		
		try {
			FileConnection fc = (FileConnection)Connector.open("file:///" + filepath);
			if (!fc.exists()) {
				System.err.println("Error: File " + filepath + " does not exist");
				throw new RuntimeException("RMS image [" + filepath + "] not found");
			}
			
			restoreRMS(fc.openDataInputStream(), true);
			fc.close();			
		} catch (IOException ioe) {
			throw new RuntimeException("ioexception: " + ioe.getMessage());
		}
	}
	
	public static void restoreRMS (DataInputStream in, boolean deleteOtherRMSes) {
		try {
			int numRMSes = ExtUtil.readInt(in);
			Vector validRMSes = new Vector();
			
			for (int i = 0; i < numRMSes; i++) {
				String rmsName = ExtUtil.readString(in);
				validRMSes.addElement(rmsName);
				
				//wipe out record store if it exists
				try {
					RecordStore rs = RecordStore.openRecordStore(rmsName, false);
					rs.closeRecordStore();
					RecordStore.deleteRecordStore(rmsName);
				} catch (RecordStoreNotFoundException rsnfe) {
					//do nothing
				}

				//inventory record ids
				int numRecords = ExtUtil.readInt(in);
				Vector recordIDs = new Vector();
				for (int j = 0; j < numRecords; j++) {
					int recordID = ExtUtil.readInt(in);
					recordIDs.addElement(new Integer(recordID));
				}

				//create record store and make record id placeholders
				RecordStore rs = RecordStore.openRecordStore(rmsName, true);
				if (!makeIDsAvailable(rs, recordIDs)) {
					System.err.println("Error: could not create record placeholders");
					throw new RuntimeException("error pre-filling record ids in rms [" + rmsName + "]");
				}

				//load record data
				for (int j = 0; j < numRecords; j++) {
					int recordID = ((Integer)recordIDs.elementAt(j)).intValue();
					int dataLength = ExtUtil.readInt(in);
					byte[] data = new byte[dataLength];
					in.read(data);
					
					rs.setRecord(recordID, data, 0, dataLength);
				}
									
				rs.closeRecordStore();
			}
		
			//optionally delete all other RMSes not in the data dump
			if (deleteOtherRMSes) {
				String[] rmses = RecordStore.listRecordStores();
				for (int i = 0; i < rmses.length; i++) {
					String rmsName = rmses[i];
					if (!validRMSes.contains(rmsName)) {
						RecordStore.deleteRecordStore(rmsName);
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("ioexception: " + ioe.getMessage());
		} catch (RecordStoreException rse) {
			throw new RuntimeException("recordstoreexception: " + rse.getMessage());		
		}
	}

	//assumes RMS records are allocated in incremental order, and start at or below the lowest record ID we need
    public static boolean makeIDsAvailable (RecordStore rs, Vector recIDs) {
    	int maxRecID = -1;
    	for (int i = 0; i < recIDs.size(); i++) {
    		maxRecID = Math.max(maxRecID, ((Integer)recIDs.elementAt(i)).intValue());
    	}
    	
    	//allocate records up to the maximum needed id
    	try {
	    	while (maxRecID >= rs.getNextRecordID()) {
	    		rs.addRecord(null, 0, 0);
	    	}
    	} catch (RecordStoreException rse) {
    		return false;
    	}
	    	
    	//test setting each record id
    	for (int i = 0; i < recIDs.size(); i++) {
    		int recID = ((Integer)recIDs.elementAt(i)).intValue();
    		try {
    			rs.setRecord(recID, null, 0, 0);
    		} catch (RecordStoreException rse) {
    			return false;
    		}
    	}
    	
    	//clean up record ids that are unused
    	try {
	    	for (RecordEnumeration e = rs.enumerateRecords(null, null, false); e.hasNextElement(); ) {
	    		int recID = e.nextRecordId();
	    		if (!recIDs.contains(new Integer(recID)) && rs.getRecord(recID) == null) {
	    			rs.deleteRecord(recID);
	    		}
	    	}
    	} catch (RecordStoreException rse) {
    		return false;
    	}
    	
    	return true;
    }
    
	public static void RMSRecoveryHook (MIDlet midlet) {
		String action = midlet.getAppProperty("RMS-Image");
		String path = midlet.getAppProperty("RMS-Image-Path");
		
		if ("dump".equals(action)) {
			System.out.println("Dumping RMS image...");
			dumpRMS(path);
		} else if ("restore".equals(action)) {
			System.out.println("Restoring RMS image...");
			restoreRMS(path);
		}		
	}
}
