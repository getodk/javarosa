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

package org.javarosa.core.util.externalizable;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.javarosa.core.util.MD5;
import org.javarosa.core.util.PrefixTree;

public class PrototypeFactory {
	public final static int CLASS_HASH_SIZE = 4;

	private Vector classes;
	private Vector hashes;

	//lazy evaluation
	private PrefixTree classNames;
	private boolean initialized;

	public PrototypeFactory () {
		this(null);
	}

	public PrototypeFactory (PrefixTree classNames) {
		this.classNames = classNames;
		initialized = false;
	}

	private void lazyInit () {
		initialized = true;

		classes = new Vector();
		hashes = new Vector();

		addDefaultClasses();

		if (classNames != null) {
			List<String> vClasses = classNames.getStrings();

			for (int i = 0; i < vClasses.size(); i++) {
				String name = (String)vClasses.get(i);
				try {
					addClass(Class.forName(name));
				} catch (ClassNotFoundException cnfe) {
					throw new CannotCreateObjectException(name + ": not found");
				}
			}
			classNames = null;
		}
	}

	private void addDefaultClasses () {
		Class[] baseTypes = {
				Object.class,
				Integer.class,
				Long.class,
				Short.class,
				Byte.class,
				Character.class,
				Boolean.class,
				Float.class,
				Double.class,
				String.class,
				Date.class
		};

		for (int i = 0; i < baseTypes.length; i++) {
			addClass(baseTypes[i]);
		}
	}

	public void addClass (Class c) {
		if (!initialized) {
			lazyInit();
		}

		byte[] hash = getClassHash(c);

		if (compareHash(hash, ExtWrapTagged.WRAPPER_TAG)) {
			throw new Error("Hash collision! " + c.getName() + " and reserved wrapper tag");
		}

		Class d = getClass(hash);
		if (d != null && d != c) {
			throw new Error("Hash collision! " + c.getName() + " and " + d.getName());
		}

		classes.addElement(c);
		hashes.addElement(hash);
	}

	public Class getClass (byte[] hash) {
		if (!initialized) {
			lazyInit();
		}

		for (int i = 0; i < classes.size(); i++) {
			if (compareHash(hash, (byte[])hashes.elementAt(i))) {
				return (Class)classes.elementAt(i);
			}
		}

		return null;
	}

	public Object getInstance (byte[] hash) {
		return getInstance(getClass(hash));
	}

	public static Object getInstance (Class c) {
		try {
			return c.newInstance();
		} catch (IllegalAccessException iae) {
			throw new CannotCreateObjectException(c.getName() + ": not accessible or no empty constructor");
		} catch (InstantiationException e) {
			throw new CannotCreateObjectException(c.getName() + ": not instantiable");
		}
	}

	public static byte[] getClassHash (Class type) {
		byte[] hash = new byte[CLASS_HASH_SIZE];
		byte[] md5 = MD5.hash(type.getName().getBytes()); //add support for a salt, in case of collision?

		for (int i = 0; i < hash.length; i++)
			hash[i] = md5[i];
		byte[] badHash = new byte[] {0,4,78,97};
		if(PrototypeFactory.compareHash(badHash, hash)) {
			System.out.println("BAD CLASS: " + type.getName());
		}

		return hash;
	}

	public static boolean compareHash (byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}

		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return false;
		}

		return true;
	}
}
