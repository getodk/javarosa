/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.services.storage;

import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.services.Logger;
import org.javarosa.core.services.storage.WrappingStorageUtility.SerializationWrapper;

/**
 * Manages StorageProviders for JavaRosa, which maintain persistent
 * data on a device.
 * 
 * Largely derived from Cell Life's RMSManager
 * 
 * @author Clayton Sims
 *
 */
public class StorageManager {
	
	private static Hashtable<String, IStorageUtility> storageRegistry = new Hashtable<String, IStorageUtility>();
	private static IStorageFactory storageFactory;
	
	/**
	 * Attempts to set the storage factory for the current environment. Will fail silently
	 * if a storage factory has already been set. Should be used by default environment.
	 * 
	 * @param fact An available storage factory.
	 */
	public static void setStorageFactory (IStorageFactory fact) {
		StorageManager.setStorageFactory(fact, false);
	}
	
	/**
	 * Attempts to set the storage factory for the current environment and fails and dies if there
	 * is already a storage factory set if specified. Should be used by actual applications who need to use
	 * a specific storage factory and shouldn't tolerate being pre-empted. 
	 * 
	 * @param fact An available storage factory.
	 * @param mustWork true if it is intolerable for another storage factory to have been set. False otherwise
	 */
	public static void setStorageFactory (IStorageFactory fact, boolean mustWork) {
		if(storageFactory == null) {
			storageFactory = fact;
		} else {
			if(mustWork) {
				Logger.die("A Storage Factory had already been set when storage factory " + fact.getClass().getName() 
						                   + " attempted to become the only storage factory", new RuntimeException("Duplicate Storage Factory set"));
			} else {
				//Not an issue
			}
		}
	}
	
	public static void registerStorage (String key, Class type) {
		registerStorage(key, key, type);
	}
	
	public static void registerStorage (String storageKey, String storageName, Class type) {
		if (storageFactory == null) {
			throw new RuntimeException("No storage factory has been set; I don't know what kind of storage utility to create. Either set a storage factory, or register your StorageUtilitys directly.");
		}
		
		registerStorage(storageKey, storageFactory.newStorage(storageName, type));
	}

	/**
	 * It is strongly, strongly advised that you do not register storage in this way.
	 * 
	 * @param key
	 * @param storage
	 */
	public static void registerStorage (String key, IStorageUtility storage) {
		storageRegistry.put(key, storage);
	}
	
	public static void registerWrappedStorage(String key, String storeName, SerializationWrapper wrapper) {
		StorageManager.registerStorage(key, new WrappingStorageUtility(storeName,wrapper,storageFactory));
	}
	
	public static IStorageUtility getStorage (String key) {
		if (storageRegistry.containsKey(key)) {
			return (IStorageUtility)storageRegistry.get(key);
		} else {
			throw new RuntimeException("No storage utility has been registered to handle \"" + key + "\"; you must register one first with StorageManager.registerStorage()");
		}
	}
	
	public static void repairAll () {
		for (Enumeration e = storageRegistry.elements(); e.hasMoreElements(); ) {
			((IStorageUtility)e.nextElement()).repair();
		}
	}
	
	public static String[] listRegisteredUtilities() {
		String[] returnVal = new String[storageRegistry.size()];
		int i = 0;
		for (Enumeration e = storageRegistry.keys(); e.hasMoreElements(); ) {
			returnVal[i] = (String)e.nextElement();
			i++;
		}
		return returnVal;
	}
	
	public static void halt() {
		for (Enumeration e = storageRegistry.elements(); e.hasMoreElements(); ) {
			((IStorageUtility)e.nextElement()).close();
		}
	}
}
