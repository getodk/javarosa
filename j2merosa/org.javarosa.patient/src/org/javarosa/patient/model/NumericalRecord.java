package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.patient.util.SelectorParser;

/**
 * NumericalRecord is a class which stores record entries of an
 * integer type.
 * 
 * @author Clayton Sims
 *
 */
public class NumericalRecord implements IPatientRecord {
	
	//Type: NumericalRecordEntry
	//Representation Invariant: entries are in order sorted from earliest to latest
	Vector numericalRecordEntries = new Vector();
	
	public NumericalRecord() {
	}
	
	/**
	 * Gets the latest measurement that has been recorded
	 * 
	 * @return The integer value of the latest recorded measurment. 
	 */
	public int getLatestMeasurement() {
		if(numericalRecordEntries.size() < 1 ) {
			//throw excepton maybe? Doesn't seem like an exceptional case
			//Then again, returning -1 seems explicitly wrong
			return -1;
		}
		else {
			NumericalRecordEntry entry = (NumericalRecordEntry) numericalRecordEntries.elementAt(numericalRecordEntries.size() -1);
			return entry.getRecordValue();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.patient.model.IPatientRecord#getHistoricalRecords(java.lang.String)
	 */
	public Vector getHistoricalRecords(String delimeters) {
		return SelectorParser.selectValues(delimeters, numericalRecordEntries);
	}
	
	/**
	 * Adds the measurement to this record.
	 * 
	 * @param entry The record entry to be added
	 */
	public void addMeasurement(NumericalRecordEntry entry) {
		int i;
		for(i = 0 ; i < numericalRecordEntries.size() ; ++i ) {
			NumericalRecordEntry curEntry = (NumericalRecordEntry) numericalRecordEntries.elementAt(i);
			if(curEntry.getEntryDate().getTime() < entry.getEntryDate().getTime() ) {
				break;
			}
		}
		numericalRecordEntries.insertElementAt(entry, i);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		numericalRecordEntries = (Vector)ExtUtil.read(in, new ExtWrapList(NumericalRecordEntry.class));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapList(numericalRecordEntries));
	}
}