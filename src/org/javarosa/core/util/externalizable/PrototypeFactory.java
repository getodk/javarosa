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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import static org.javarosa.xpath.expr.DigestAlgorithm.MD5;

public class PrototypeFactory {
    private static final Logger logger = LoggerFactory.getLogger(PrototypeFactory.class);

    public final static int CLASS_HASH_SIZE = 4;

    private final Vector<Class> classes = new Vector<>();
    private final Vector<byte[]> hashes = new Vector<>();

    //lazy evaluation
    private List<String> classNames;
    private boolean initialized;

    public PrototypeFactory () {
        this(null);
    }

    public PrototypeFactory (List<String> classNames) {
        this.classNames = classNames;
        initialized = false;
    }

    private void lazyInit () {
        initialized = true;

        addDefaultClasses();

        if (classNames != null) {
            for (String className : classNames) {
                try {
                    addClass(Class.forName(className));
                } catch (ClassNotFoundException cnfe) {
                    throw new CannotCreateObjectException(className + ": not found");
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

        for (Class baseType : baseTypes) {
            addClass(baseType);
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
            if (compareHash(hash, hashes.elementAt(i))) {
                return classes.elementAt(i);
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
        byte[] md5 = MD5.digest(type.getName());

        System.arraycopy(md5, 0, hash, 0, hash.length);
        byte[] badHash = new byte[] {0,4,78,97};
        if(PrototypeFactory.compareHash(badHash, hash)) {
            logger.info("BAD CLASS: {}", type.getName());
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
