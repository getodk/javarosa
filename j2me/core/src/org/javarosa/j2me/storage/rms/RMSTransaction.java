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
		this.tx_id = new Random().nextInt();
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

