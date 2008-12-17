package org.javarosa.patient.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class ImmunizationAnswerData  implements IAnswerData {
	
	ImmunizationData data;
	
	public ImmunizationAnswerData() {
		
	}
	
	public ImmunizationAnswerData(ImmunizationData data ) {
		this.data = data;
	}
	
	public String getDisplayText() {
		return "Data Table";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return data;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(Object o) {
		data = (ImmunizationData)o;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(ImmunizationData o) {
		data = o;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		data = new ImmunizationData();
		data.readExternal(in, pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		data.writeExternal(out);
	}

	public IAnswerData clone() {
		return new ImmunizationAnswerData(data.clone());
	}
}
