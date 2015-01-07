/**
 * 
 */
package org.javarosa.core.services.storage;

import org.javarosa.core.services.storage.WrappingStorageUtility.SerializationWrapper;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author ctsims
 *
 */
public class FauxIndexedPersistable implements Persistable, IMetaData {

	Persistable p;
	SerializationWrapper w;
	IMetaData m;
	public FauxIndexedPersistable(Persistable p, SerializationWrapper w) {
		this.p = p;
		this.w = w;
		this.m = null;
	}
	
	public FauxIndexedPersistable(Persistable p, SerializationWrapper w, IMetaData m) {
		this.p = p;
		this.w = w;
		this.m = m;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.Persistable#getID()
	 */
	public int getID() {
		return p.getID();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.Persistable#setID(int)
	 */
	public void setID(int ID) {
		p.setID(ID);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		w.readExternal(in, pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		w.writeExternal(out);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IMetaData#getMetaData()
	 */
	public HashMap<String,Object> getMetaData() {
		if(m != null) {return m.getMetaData();}
		throw new RuntimeException("Attempt to index unindexible " + p.getClass().getName());
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IMetaData#getMetaData(java.lang.String)
	 */
	public Object getMetaData(String fieldName) {
		if(m != null) {return m.getMetaData(fieldName);}
		throw new RuntimeException("Attempt to index unindexible " + p.getClass().getName());
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IMetaData#getMetaDataFields()
	 */
	public String[] getMetaDataFields() {
		if(m != null) {return m.getMetaDataFields();}
		throw new RuntimeException("Attempt to index unindexible " + p.getClass().getName());
	}

}
