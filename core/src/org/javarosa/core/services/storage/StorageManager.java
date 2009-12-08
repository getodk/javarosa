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

/* droos: what is the future of this class? i think it will take a different form with the new storage API */

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
	
	public static void setStorageFactory (IStorageFactory fact) {
		storageFactory = fact;
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
	
	public static void registerStorage (String key, IStorageUtility storage) {
		storageRegistry.put(key, storage);
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
}
