/**
 * 
 */
package org.javarosa.core.reference;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author ctsims
 *
 */
public class RootTranslator implements ReferenceFactory, Externalizable {
	
	public String prefix;
	public String translatedPrefix;
	
	/**
	 * Serialization only!
	 */
	public RootTranslator() {
		
	}
	
	public RootTranslator(String prefix, String translatedPrefix) {
		//TODO: Manage semantics of "ends with /" etc here?
		this.prefix = prefix;
		this.translatedPrefix = translatedPrefix;
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Root#derive(java.lang.String)
	 */
	public Reference derive(String URI) throws InvalidReferenceException {
		return ReferenceManager._().DeriveReference(translatedPrefix + URI.substring(prefix.length()));
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Root#derive(java.lang.String, java.lang.String)
	 */
	public Reference derive(String URI, String context) throws InvalidReferenceException {
		return ReferenceManager._().DeriveReference(URI, translatedPrefix + context.substring(prefix.length()));
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Root#derives(java.lang.String)
	 */
	public boolean derives(String URI) {
		if(URI.startsWith(prefix)) {
			return true;
		} else{
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		prefix = ExtUtil.readString(in);
		translatedPrefix = ExtUtil.readString(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {		
		ExtUtil.writeString(out, prefix);
		ExtUtil.writeString(out, translatedPrefix);
	}

}
