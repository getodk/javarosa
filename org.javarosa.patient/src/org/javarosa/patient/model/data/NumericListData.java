package org.javarosa.patient.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patient.model.IPatientRecord;
import org.javarosa.patient.model.NumericalRecordEntry;
import org.javarosa.patient.util.DateValueTuple;
import org.javarosa.patient.util.SelectorParser;

/**
 * @author Clayton Sims
 *
 */
public class NumericListData implements IAnswerData, IPatientRecord {

	Vector valueList = new Vector();
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
	public String getDisplayText() {
		return "Data List";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return valueList;
	}
	

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(Object o) { 
		valueList = (Vector)o;
	}
	
	/**
	 * Gets the latest measurement that has been recorded
	 * 
	 * @return The integer value of the latest recorded measurment. 
	 */
	public int getLatestMeasurement() {
		if(valueList.size() < 1 ) {
			//throw excepton maybe? Doesn't seem like an exceptional case
			//Then again, returning -1 seems explicitly wrong
			return -1;
		}
		else {
			DateValueTuple entry = (DateValueTuple) valueList.elementAt(valueList.size() -1);
			return entry.value;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.patient.model.IPatientRecord#getHistoricalRecords(java.lang.String)
	 */
	public Vector getHistoricalRecords(String delimeters) {
		return SelectorParser.selectValues(delimeters, valueList);
	}
	
	/**
	 * Adds the measurement to this record.
	 * 
	 * @param entry The record entry to be added
	 */
	public void addMeasurement(DateValueTuple entry) {
		int i;
		for(i = 0 ; i < valueList.size() ; ++i ) {
			DateValueTuple curEntry = (DateValueTuple) valueList.elementAt(i);
			if(curEntry.date.getTime() < entry.date.getTime() ) {
				break;
			}
		}
		valueList.insertElementAt(entry, i);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		valueList = ExternalizableHelper.readExternal(in, DateValueTuple.class);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	 public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeExternal(valueList, out);
	}

}
