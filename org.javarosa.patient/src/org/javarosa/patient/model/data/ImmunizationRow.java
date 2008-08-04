/**
 * 
 */
package org.javarosa.patient.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * @author Clayton Sims
 *
 */
public class ImmunizationRow implements Externalizable {
	private String vaccinationName;
	int[] vaccinationDoses = new int[] {-1, -1, -1, -1, -1};
	Date[] vaccinationDates = new Date[5];
	
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
	
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		vaccinationName = ExternalizableHelper.readUTF(in);
		
		for(int i = 0 ; i < 5 ; i++) {
			vaccinationDoses[i] = in.readInt();
		}
		for(int i = 0 ; i < 5 ; i++) {
			vaccinationDates[i] = ExternalizableHelper.readDate(in);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out, vaccinationName);
		
		for(int i = 0 ; i < 5 ; i++) {
			out.writeInt(vaccinationDoses[i]);
		}
		for(int i = 0 ; i < 5 ; i++) {
			ExternalizableHelper.writeDate(out, vaccinationDates[i]);
		}
	}

}
