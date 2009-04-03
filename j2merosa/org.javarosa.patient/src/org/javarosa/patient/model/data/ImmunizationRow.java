/**
 * 
 */
package org.javarosa.patient.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 *
 */
public class ImmunizationRow implements Externalizable {
	private String vaccinationName;
	int[] vaccinationDoses = new int[] {-1, -1, -1, -1, -1};
	Date[] vaccinationDates = new Date[5];
	boolean[] cellEnabled = new boolean[] {true,true,true,true,true};
	
	public ImmunizationRow() {
		
	}
	
	public ImmunizationRow(String name) {
		this.vaccinationName = name;
	}
	
	public void setDose(int doseType, int doseStatus, Date doseDate) {
		if(doseType >= 0 && doseType <=4 && doseStatus >= 1 && doseStatus <=4) {
			vaccinationDoses[doseType] = doseStatus;
			if(doseStatus == 4) {
				vaccinationDates[doseType] = doseDate;
			}
		}
	}
	
	public void setVaccinationDose(int doseType, int doseStatus) {
		setDose(doseType, doseStatus, null);
	}
	
	public Date getDate(int doseType) {
		return vaccinationDates[doseType];
	}
	
	public int getStatus(int doseType) {
		return vaccinationDoses[doseType];
	}
	
	/**
	 * @return the cellEnabled
	 */
	public boolean getCellEnabled(int column) {
		return cellEnabled[column];
	}

	/**
	 * @param cellEnabled the cellEnabled to set
	 */
	public void setCellEnabled(int column, boolean cellEnabled) {
		this.cellEnabled[column] = cellEnabled;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		vaccinationName = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
		
		for(int i = 0 ; i < 5 ; i++) {
			vaccinationDoses[i] = in.readInt();
		}
		for(int i = 0 ; i < 5 ; i++) {
			vaccinationDates[i] = (Date)ExtUtil.read(in, new ExtWrapNullable(Date.class));
		}
		for(int i = 0 ; i < 5 ; i++) {
			cellEnabled[i] = in.readBoolean();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapNullable(vaccinationName));
		
		for(int i = 0 ; i < 5 ; i++) {
			out.writeInt(vaccinationDoses[i]);
		}
		for(int i = 0 ; i < 5 ; i++) {
			ExtUtil.write(out, new ExtWrapNullable(vaccinationDates[i]));
		}
		for(int i = 0 ; i < 5 ; i++) {
			out.writeBoolean(cellEnabled[i]);
		}
	}
	public ImmunizationRow clone() {
		ImmunizationRow clone = new ImmunizationRow();
		int size = vaccinationDoses.length;
		clone.vaccinationDoses = new int[size];
		for(int i = 0; i < size ; ++i) {
			clone.vaccinationDoses[i] = this.vaccinationDoses[i];
		}
		size = vaccinationDates.length;
		clone.vaccinationDates = new Date[size];
		for(int i = 0; i < size ; ++i) {
			clone.vaccinationDates[i] = this.vaccinationDates[i];
		}
		size = cellEnabled.length;
		clone.cellEnabled = new boolean[size];
		for(int i = 0; i < size ; ++i) {
			clone.cellEnabled[i] = this.cellEnabled[i];
		}
		return clone;
	}
}
