package org.javarosa.j2me.storage.rms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.log.WrappedException;
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
			throw new WrappedException("ERROR: failed to rollback transaction! you should assume your application data has become inconsistent/corrupted. sorry...", e);
		} finally {
			clearTx();
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
		Hashtable<String, RMSStorageUtility> rmses = new Hashtable<String, RMSStorageUtility>();
		
		for (Enumeration e = tx.tx_records.keys(); e.hasMoreElements(); ) {
			int tx_cache_id = tx.tx_records.get(e.nextElement());
			
			if (restore) {
				TxCacheEntry entry = (TxCacheEntry)getCacheRMS().read(tx_cache_id);
				
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
			
			getCacheRMS().remove(tx_cache_id);
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

