/**
 * 
 */
package org.javarosa.patient.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
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
		vaccinationName = ExternalizableHelperDeprecated.readUTF(in);
		
		for(int i = 0 ; i < 5 ; i++) {
			vaccinationDoses[i] = in.readInt();
		}
		for(int i = 0 ; i < 5 ; i++) {
			vaccinationDates[i] = ExternalizableHelperDeprecated.readDate(in);
		}
		for(int i = 0 ; i < 5 ; i++) {
			cellEnabled[i] = in.readBoolean();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelperDeprecated.writeUTF(out, vaccinationName);
		
		for(int i = 0 ; i < 5 ; i++) {
			out.writeInt(vaccinationDoses[i]);
		}
		for(int i = 0 ; i < 5 ; i++) {
			ExternalizableHelperDeprecated.writeDate(out, vaccinationDates[i]);
		}
		for(int i = 0 ; i < 5 ; i++) {
			out.writeBoolean(cellEnabled[i]);
		}
	}

}
