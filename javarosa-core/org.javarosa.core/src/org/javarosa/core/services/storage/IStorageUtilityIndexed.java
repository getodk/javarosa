package org.javarosa.core.services.storage;

import java.util.Vector;

import org.javarosa.core.util.externalizable.Externalizable;

/* TEMPORARY / EXPERIMENTAL */

public interface IStorageUtilityIndexed extends IStorageUtility {

	Vector getIDsForValue (String fieldName, Object value);
	Externalizable getRecordForValue (String fieldName, Object value);
	
}
