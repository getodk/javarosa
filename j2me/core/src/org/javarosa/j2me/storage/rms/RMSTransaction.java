package org.javarosa.j2me.storage.rms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.util.MathUtils;

/**
 * This class enables basic transactions when writing to RMS -- that is, you can begin a transaction,
 * modify records in RMS, and optionally rollback to the state before you began the transaction, undoing
 * all your record operations.
 * 
 * This works behind the scenes by copying the original version of each touched record to a temporary
 * RMS, and copying them all back to their original RMSes in the event of rollback. This means:
 * 
 * a) the transaction is not guaranteed to be atomic or succeed, but it makes a best-faith effort to do
 * so. the integrity of the RMSStorageUtilitys is maintained just as with non-transactional access, but
 * in the event of an unforeseen transaction error, the contents of the records may not be as you expect
 * (for example, referential integrity between records may be violated, even though each individual record
 * is intact).
 * 
 * b) the state of the underlying RMSes will not be identical after rollback (records may have different
 * raw RMS record IDs than when they started), but should look the same from the perspective of a user of
 * the IStorageUtility interface.
 * 
 * transaction support is thread-safe, in that multiple transactions can be active in multiple threads at
 * once (at most one transaction per thread). however, multiple threads accessing records in the same RMS
 * while at least one transaction is active amongst the threads may lead to unpredictable behavior (i.e., 
 * thread 2's record changes may be overwritten if thread 1's transaction is rolled back). it is the
 * responsibility of the threads to coordinate not stepping on each other's toes.
 * 
 * beginTransaction(), commitTransaction(), and rollbackTransaction() are the only methods you need be
 * concerned with as an end-user.
 * 
 * once a transaction is active, you can use all the normal IStorageUtility methods as usual, without any
 * change.
 * 
 * beginTransaction() starts a transaction. at most one active transaction per thread is allowed. once the
 * transaction is active, every write/add/update/remove action by this thread in ANY RMS is logged and
 * will be undone if the transaction is rolled back. you must ensure that this call is ALWAYS matched by
 * exactly ONE call to commitTransaction() or rollbackTransaction(), through all possible code and
 * exception paths. leaving a transaction open indefinitely will seriously tax your device storage, and
 * none of your record changes will stick.
 * 
 * commitTransaction() ends the transaction, causing the changes to the RMS to 'stick'. the backup records
 * are purged.
 * 
 * rollbackTransaction() undoes the changes since beginTransaction(). the transaction is ended and the
 * RMSUtilities should have the same record IDs and contents as before the transaction. there is NO
 * provision for auto-rollback (such as in the event of a runtime exception in the storage layer); YOU are
 * responsible for initiating rollback.
 * 
 * when the application is booted, it attempts to rollback any unterminated transactions. this is to
 * protect against, say, pulling the battery out while a transaction is in progress, or unexpected
 * application crash.
 * 
 * failure modes:
 * 
 * there is a chance that committing or rolling back the transaction will fail due to an underlying RMS
 * error. if this happens, the records of your application's RMS will likely be in an inconsistent state,
 * and you will probably be hosed. the best bet will be to re-initialize your app from scratch, from an
 * authoritative source. committing only deletes RMS records, thus (i assume) is less likely to fail
 * 
 * while a transaction is in progress, a failure may occur when trying to make the backup during a normal
 * RMS operation (such as a write()). if this happens, an exception is thrown, and the operation will be
 * aborted (i.e., the record was not written). HOWEVER, the transaction is still in progress, and you must
 * decide what to do. you could attempt to keep writing records, for example, though the most logical choice
 * is to initiate rollback.
 * 
 * @author Drew Roos
 *
 */
public class RMSTransaction {

	private class RMSRec {
		String rms;
		int id;
		
		public RMSRec (String rms, int id) {
			this.rms = rms;
			this.id = id;
		}
		
		public int hashCode () {
			return this.rms.hashCode() ^ new Integer(this.id).hashCode();
		}
		
		public boolean equals (Object o) {
			return (o instanceof RMSRec) && this.hashCode() == o.hashCode();
		}
	}
	
	int tx_id;
	Hashtable<RMSRec, Integer> tx_records;

	public RMSTransaction () {
		this.tx_id = MathUtils.getRand().nextInt();
		this.tx_records = new Hashtable<RMSRec, Integer>();
	}
	
	public boolean isRecordTouched (String rmsName, int rec_id) {
		return tx_records.containsKey(new RMSRec(rmsName, rec_id));
	}
	
	public void recordTouched (String rmsName, int rec_id, int tx_cache_id) {
		tx_records.put(new RMSRec(rmsName, rec_id), new Integer(tx_cache_id));
	}
	
	//============ static tx manager ==============
	
	static final String CACHE_RMS = "TX_CACHE";

	private static RMSStorageUtility cacheRMS = null;
	
	private static Hashtable<Thread, RMSTransaction> transactions = new Hashtable<Thread, RMSTransaction>();
		
	public static void beginTransaction () {
		synchronized (transactions) {
			if (transactions.containsKey(Thread.currentThread())) {
				throw new FatalException("transaction is already in progress for current thread");
			}
			transactions.put(Thread.currentThread(), new RMSTransaction());
		}
	}
	
	public static void commitTransaction () {
		RMSTransaction tx = getTx();
		if (tx == null) {
			throw new RuntimeException("no transaction is active for the current thread");
		}
		
		try {
			purgeCached(tx);
		} catch (Exception e) {
			Logger.log("rms-tx", "critical failure during transaction commit");
			throw new WrappedException("ERROR: failed to commit transaction! you should assume your application data has become inconsistent/corrupted. sorry...", e);
		} finally {
			clearTx();
		}
	}
	
	public static void rollbackTransaction () {
		RMSTransaction tx = getTx();
		if (tx == null) {
			throw new RuntimeException("no transaction is active for the current thread");
		}
		
		try {
			restoreCached(tx);
		} catch (Exception e) {
			Logger.log("rms-tx", "critical failure during transaction rollback");
			throw new WrappedException("ERROR: failed to rollback transaction! you should assume your application data has become inconsistent/corrupted. sorry...", e);
		} finally {
			clearTx();
		}
	}
		
	public static void cleanup () {
		if (anyTxOpen()) {
			throw new RuntimeException("should only be called on a fresh boot with no transactions open");
		}
		
		if (getCacheRMS().getNumRecords() == 0)
			return;
		
		Logger.log("rms-tx", "incomplete transactions found; cleaning up...");
		try {		
			Vector<Integer> tx_cache_ids = new Vector<Integer>();
			for (IStorageIterator ii = getCacheRMS().iterate(); ii.hasMore(); ) {
				tx_cache_ids.addElement(new Integer(ii.nextID()));
			}
			
			processCache(tx_cache_ids.elements(), true);
		} catch (Exception e) {
			Logger.log("rms-tx", "error during transaction clean-up; major data corruption");
			throw new FatalException("can't clean up aborted transactions; data corruption");
		}
	}
	
	public static RMSTransaction getTx () {
		synchronized(transactions) {
			return transactions.get(Thread.currentThread());
		}
	}
	
	private static void clearTx () {
		synchronized(transactions) {
			transactions.remove(Thread.currentThread());
		}		
	}
	
	public static boolean anyTxOpen () {
		synchronized(transactions) {
			return transactions.size() > 0;
		}
	}
	
	public static RMSStorageUtility getCacheRMS () {
		if (cacheRMS == null)
			cacheRMS = new RMSStorageUtility(RMSTransaction.CACHE_RMS, TxCacheEntry.class);
		return cacheRMS;
	}
	
	private static void purgeCached (RMSTransaction tx) {
		processCache(tx, false);
	}

	private static void restoreCached (RMSTransaction tx) {
		processCache(tx, true);
	}
		
	private static void processCache (RMSTransaction tx, boolean restore) {	
		processCache(tx.tx_records.elements(), restore);
	}
		
	private static void processCache (Enumeration tx_cache_ids, boolean restore) {
		Hashtable<String, RMSStorageUtility> rmses = new Hashtable<String, RMSStorageUtility>();
		
		while (tx_cache_ids.hasMoreElements()) {
			int tx_cache_id = ((Integer)tx_cache_ids.nextElement()).intValue();
			
			if (restore) {
				TxCacheEntry entry = (TxCacheEntry)getCacheRMS().read(tx_cache_id);
				revertRecord(entry, rmses);
			}
			
			getCacheRMS().remove(tx_cache_id);
		}		
	}

	private static void revertRecord (TxCacheEntry entry, Hashtable<String, RMSStorageUtility> rmses) {	
		RMSStorageUtility rms = rmses.get(entry.rms);
		if (rms == null) {
			rms = new RMSStorageUtility(entry.rms, RawRecord.class);
			rmses.put(entry.rms, rms);
		}
			
		if (entry.data == null) {
			if (rms.exists(entry.rec_id)) {
				rms.remove(entry.rec_id);
			}
		} else {
			if (!rms.exists(entry.rec_id) || !cmpData(entry.data, rms.readBytes(entry.rec_id))) {
				try {
					rms.write(entry.toRawRec());
				} catch (StorageFullException sfe) {
					throw new WrappedException(sfe);
				}
			}
		}
	}
	
	private static boolean cmpData (byte[] a, byte[] b) {
		if (a.length == b.length) {
			for (int i = 0; i < a.length; i++) {
				if (a[i] != b[i])
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
}

