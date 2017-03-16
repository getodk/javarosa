/**
 *
 */
package org.javarosa.core.services.storage;

import java.util.HashMap;

/**
 * An internal-use class to keep track of metadata records without requiring
 * the original object to remain in memory
 *
 * @author ctsims
 *
 */
public class MetaDataWrapper implements IMetaData {
	private HashMap<String, Object> data;

	public MetaDataWrapper(HashMap<String, Object> data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IMetaData#getMetaDataFields()
	 */
	public String[] getMetaDataFields() {
		String[] fields = new String[data.size()];
		int count = 0;
		for (String field : data.keySet()) {
			fields[count] = field;
		}
		return fields;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IMetaData#getMetaData()
	 */
	public HashMap<String,Object> getMetaData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IMetaData#getMetaData(java.lang.String)
	 */
	public Object getMetaData(String fieldName) {
		return data.get(fieldName);
	}

}
