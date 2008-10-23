package org.javarosa.core.services.storage.utilities;


public interface IRecordStoreEnumeration {

	public void destroy();

	public boolean hasNextElement();
	
	public boolean hasPreviousElement();

	public boolean isKeptUpdated();

	public void keepUpdated(boolean arg0);

	public byte[] nextRecord() throws RecordStorageException;

	public int nextRecordId() throws RecordStorageException;

	public int numRecords();

	public byte[] previousRecord() throws RecordStorageException;

	public int previousRecordId() throws RecordStorageException;

	public void rebuild();

	public void reset();

}
