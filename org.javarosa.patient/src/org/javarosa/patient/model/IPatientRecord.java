package org.javarosa.patient.model;

import java.util.Vector;

import org.javarosa.core.services.storage.utilities.Externalizable;

/**
 * IPatientRecord is a storage location for historical
 * patient record entries. 
 * 
 * @author Clayton Sims
 *
 */
public interface IPatientRecord extends Externalizable {
	
	/**
	 * Returns a filtered set of historical record entries
	 * 
	 * @param selector The Selector string to be used to
	 * filter the entries
	 * @return a subset of IRecordEntry objects which correspond
	 * to the set of data specified by the selector
	 */
	public Vector getHistoricalRecords(String selector);
}
