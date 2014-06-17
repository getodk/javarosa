/**
 *
 */
package org.javarosa.core.util;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author ctsims
 *
 */
public class CacheTable<K> {
	int totalAdditions = 0;

	private Hashtable<Integer, WeakReference> currentTable;

	private static Vector<WeakReference> caches = new Vector<WeakReference>(1);

	private static Thread cleaner = new Thread(new Runnable() {
		public void run() {
			Vector<Integer> toRemove = new Vector<Integer>();
			while(true) {
				try {
					toRemove.removeAllElements();
					for (int i = 0; i < caches.size(); ++i) {
						CacheTable cache = (CacheTable) caches.elementAt(i).get();
						if (cache == null) {
							toRemove.addElement(DataUtil.integer(i));
						} else {
							Hashtable<Integer, WeakReference> table = cache.currentTable;
							for (Enumeration en = table.keys(); en.hasMoreElements();) {
								Object key = en.nextElement();

								synchronized(cache) {
									//See whether or not the cached reference has been cleared by the GC
									if (((WeakReference) table.get(key)).get() == null) {
											//If so, remove the entry, it's no longer useful.
											table.remove(key);
									}
								}
							}

							synchronized(cache) {
								//See if our current size is 25% the size of the largest size we've been
								//and compact (clone to a new table) if so, since the table maintains the
								//largest size it has ever been.
								//TODO: 50 is a super arbitrary upper bound
								if(cache.totalAdditions > 50 && cache.totalAdditions - cache.currentTable.size() > (cache.currentTable.size() >> 2) ) {
									Hashtable newTable = new Hashtable(cache.currentTable.size());
									int oldMax = cache.totalAdditions;
									for (Enumeration en = table.keys(); en.hasMoreElements();) {
										Object key = en.nextElement();
										newTable.put(key, cache.currentTable.get(key));
									}
									cache.currentTable = newTable;
									cache.totalAdditions = cache.currentTable.size();
								}
							}

						}
					}
					for (int id = toRemove.size() - 1; id >= 0; --id) {
						caches.removeElementAt(toRemove.elementAt(id));
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	});

	private static void registerCache(CacheTable table) {
		caches.addElement(new WeakReference(table));
		synchronized(cleaner) {
			if(!cleaner.isAlive()) {
				cleaner.start();
			}
		}
	}

	public CacheTable() {
		super();
		currentTable = new Hashtable<Integer, WeakReference>();
		registerCache(this);
	}

	public K intern(K k) {
		synchronized(this) {
			int hash = k.hashCode();
			K nk = retrieve(hash);
			if(nk == null) {
				register(hash, k);
				return k;
			}
			if(k.equals(nk)) {
				return nk;
			} else {
				//Collision. We should deal with this better for interning (and not manually caching) tables.
			}
			return k;
		}
	}


	public K retrieve(int key) {
		synchronized(this) {
			if(!currentTable.containsKey(DataUtil.integer(key))) { return null; }
			K retVal = (K)currentTable.get(DataUtil.integer(key)).get();
			if(retVal == null) { currentTable.remove(DataUtil.integer(key)); }
			return retVal;
		}
	}

	public void register(int key, K item) {
		synchronized(this) {
			currentTable.put(DataUtil.integer(key), new WeakReference(item));
			totalAdditions++;
		}
	}
}
