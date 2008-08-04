package org.javarosa.patient.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * @author Clayton Sims
 *
 */
public class ImmunizationData implements IAnswerData {

	/** ImmunizationRow */
	Vector rows;
	
	public String getDisplayText() {
		return "Data Table";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return rows;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(Object o) {
		rows = (Vector)o;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		rows = ExternalizableHelper.readExternal(in,ImmunizationRow.class);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeExternal(rows, out);
	}

}
