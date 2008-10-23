package org.javarosa.core.services.storage.utilities;

public interface IRecordStorage {
	
	public void openAsRecordStorage(String name, boolean createIfNotExist) throws RecordStorageException;
	
	public void closeRecordStore() throws RecordStorageException;
	
	public int getNextRecordID() throws RecordStorageException;

	public void addRecord(byte[] data, int i, int length) throws RecordStorageException;

	public void setRecord(int recordId, byte[] data, int i, int length) throws RecordStorageException;

	public void deleteRecord(int recordId) throws RecordStorageException;

	public void deleteRecordStore() throws RecordStorageException;

	public byte[] getRecord(int recordId) throws RecordStorageException;

	public int getNumRecords() throws RecordStorageException;

	public IRecordStoreEnumeration enumerateRecords() throws RecordStorageException;

	public float getSize() throws RecordStorageException;
	
	public float getSizeAvailable() throws RecordStorageException;
}
