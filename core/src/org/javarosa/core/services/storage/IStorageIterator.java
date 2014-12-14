package org.javarosa.core.services.storage;

import org.javarosa.core.util.Iterator;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * Interface for iterating through a set of records from an IStorageUtility
 */
public interface IStorageIterator<E extends Externalizable> extends Iterator<E>{

}
