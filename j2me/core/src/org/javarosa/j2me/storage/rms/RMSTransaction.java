package org.javarosa.j2me.storage.rms;

import java.util.Hashtable;
import java.util.Random;

import org.javarosa.core.log.FatalException;

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
		tx_records.put(new RMSRec(rmsName, rec_id), tx_cache_id);
	}
	
	//============ static tx manager ==============
	
	static final String CACHE_RMS = "TX_CACHE";

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
		
		purgeCached(tx);
		clearTx();
	}
	
	public static void rollbackTransaction () {
		RMSTransaction tx = getTx();
		if (tx == null) {
			throw new RuntimeException("no transaction is active for the current thread");
		}
		
		restoreCached(tx);
		clearTx();
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
	
	private static void purgeCached (RMSTransaction tx) {
		
	}

	private static void restoreCached (RMSTransaction tx) {
		
	}
}
