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

package org.javarosa.core.util.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.javarosa.core.util.OrderedMap;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapBase;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapMapPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableWrapper;
import org.javarosa.core.util.externalizable.PrototypeFactory;import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalizableTest {
    private static final Logger logger = LoggerFactory.getLogger(ExternalizableTest.class);


    @Test
    public void doTests () {
        //base types (built-in + externalizable)
        testExternalizable("string", String.class);
        testExternalizable(new Byte((byte)0), Byte.class);
        testExternalizable(new Byte((byte)0x55), Byte.class);
        testExternalizable(new Byte((byte)0xe9), Byte.class);
        testExternalizable(new Short((short)0), Short.class);
        testExternalizable(new Short((short)-12345), Short.class);
        testExternalizable(new Short((short)12345), Short.class);
        testExternalizable(new Integer(0), Integer.class);
        testExternalizable(new Integer(1234567890), Integer.class);
        testExternalizable(new Integer(-1234567890), Integer.class);
        testExternalizable(new Long(0), Long.class);
        testExternalizable(new Long(1234567890123456789l), Long.class);
        testExternalizable(new Long(-1234567890123456789l), Long.class);
        testExternalizable(Boolean.TRUE, Boolean.class);
        testExternalizable(Boolean.FALSE, Boolean.class);
        testExternalizable(new Character('e'), Character.class);
        testExternalizable(new Float(123.45e6), Float.class);
        testExternalizable(new Double(123.45e6), Double.class);
        testExternalizable(new Date(), Date.class);
        testExternalizable(new SampleExtz("your", "mom"), SampleExtz.class);

        //base wrapper (end user will never use)
        testExternalizable("string", new ExtWrapBase(String.class));
        testExternalizable(new ExtWrapBase("string"), String.class);

        //nullables on base types
        testExternalizable(new ExtWrapNullable((String)null), new ExtWrapNullable(String.class));
        testExternalizable(new ExtWrapNullable("string"), new ExtWrapNullable(String.class));
        testExternalizable(new ExtWrapNullable((Integer)null), new ExtWrapNullable(Integer.class));
        testExternalizable(new ExtWrapNullable(new Integer(17)), new ExtWrapNullable(Integer.class));
        testExternalizable(new ExtWrapNullable((SampleExtz)null), new ExtWrapNullable(SampleExtz.class));
        testExternalizable(new ExtWrapNullable(new SampleExtz("hi", "there")), new ExtWrapNullable(SampleExtz.class));

        //lists of base types
        List<Integer> v = new ArrayList<Integer>();
        v.add(new Integer(27));
        v.add(new Integer(-73));
        v.add(new Integer(1024));
        v.add(new Integer(66066066));
        testExternalizable(new ExtWrapList(v), new ExtWrapList(Integer.class));

        List<String> vs = new ArrayList<String>();
        vs.add("alpha");
        vs.add("beta");
        vs.add("gamma");
        testExternalizable(new ExtWrapList(vs), new ExtWrapList(String.class));

        List<Object> w = new ArrayList<Object>();
        w.add(new SampleExtz("where", "is"));
        w.add(new SampleExtz("the", "beef"));
        testExternalizable(new ExtWrapList(w), new ExtWrapList(SampleExtz.class));

        //nullable lists; lists of nullables (no practical use)
        testExternalizable(new ExtWrapNullable(new ExtWrapList(v)), new ExtWrapNullable(new ExtWrapList(Integer.class)));
        testExternalizable(new ExtWrapNullable((ExtWrapList)null), new ExtWrapNullable(new ExtWrapList(Integer.class)));
        testExternalizable(new ExtWrapList(v, new ExtWrapNullable()), new ExtWrapList(new ExtWrapNullable(Integer.class)));

        //empty lists (base types)
        testExternalizable(new ExtWrapList(new ArrayList<String>()), new ExtWrapList(String.class));
        testExternalizable(new ExtWrapList(new ArrayList(), new ExtWrapBase(Integer.class)), new ExtWrapList(String.class)); //sub-types don't matter for empties

        //lists of lists (including empties)
        ArrayList x = new ArrayList();
        x.add(new Integer(-35));
        x.add(new Integer(-31415926));
        ArrayList y = new ArrayList();
        y.add(v);
        y.add(x);
        y.add(new ArrayList());
        testExternalizable(new ExtWrapList(y, new ExtWrapList()), new ExtWrapList(new ExtWrapList(Integer.class))); //risky to not specify 'leaf' type (Integer), but works in limited situations
        testExternalizable(new ExtWrapList(new ArrayList(), new ExtWrapList()), new ExtWrapList(new ExtWrapList(Integer.class))); //same as above

        //tagged base types
        testExternalizable(new ExtWrapTagged("string"), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new Integer(5000)), new ExtWrapTagged());
        //tagged custom type
        PrototypeFactory pf = new PrototypeFactory();
        pf.addClass(SampleExtz.class);
        testExternalizable(new ExtWrapTagged(new SampleExtz("bon", "jovi")), new ExtWrapTagged(), pf);
        //tagged list (base type)
        testExternalizable(new ExtWrapTagged(new ExtWrapList(v)), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new ExtWrapList(w)), new ExtWrapTagged(), pf);
        //tagged nullables and compound lists
        testExternalizable(new ExtWrapTagged(new ExtWrapNullable("string")), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new ExtWrapNullable((String)null)), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new ExtWrapList(y, new ExtWrapList(Integer.class))), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new ExtWrapList(new ArrayList(), new ExtWrapList(Integer.class))), new ExtWrapTagged());

        //polymorphic lists
        List a = new ArrayList();
        a.add(new Integer(47));
        a.add("string");
        a.add(Boolean.FALSE);
        a.add(new SampleExtz("hello", "dolly"));
        testExternalizable(new ExtWrapListPoly(a), new ExtWrapListPoly(), pf);
        testExternalizable(new ExtWrapTagged(new ExtWrapListPoly(a)), new ExtWrapTagged(), pf);
        //polymorphic list with complex sub-types
        a.add(new ExtWrapList(y, new ExtWrapList(Integer.class))); //note: must manually wrap children in polymorphic lists
        testExternalizable(new ExtWrapListPoly(a), new ExtWrapListPoly(), pf);
        testExternalizable(new ExtWrapListPoly(new ArrayList()), new ExtWrapListPoly());

        //hashtables
        OrderedMap oh = new OrderedMap();
        testExternalizable(new ExtWrapMap(oh), new ExtWrapMap(String.class, Integer.class, ExtWrapMap.TYPE_ORDERED));
        testExternalizable(new ExtWrapMapPoly(oh), new ExtWrapMapPoly(Date.class, true));
        testExternalizable(new ExtWrapTagged(new ExtWrapMap(oh)), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new ExtWrapMapPoly(oh)), new ExtWrapTagged());
        oh.put("key1", new SampleExtz("a", "b"));
        oh.put("key2", new SampleExtz("c", "d"));
        oh.put("key3", new SampleExtz("e", "f"));
        testExternalizable(new ExtWrapMap(oh), new ExtWrapMap(String.class, SampleExtz.class, ExtWrapMap.TYPE_ORDERED), pf);
        testExternalizable(new ExtWrapTagged(new ExtWrapMap(oh)), new ExtWrapTagged(), pf);

        HashMap h = new HashMap();
        testExternalizable(new ExtWrapMap(h), new ExtWrapMap(String.class, Integer.class));
        testExternalizable(new ExtWrapMapPoly(h), new ExtWrapMapPoly(Date.class));
        testExternalizable(new ExtWrapTagged(new ExtWrapMap(h)), new ExtWrapTagged());
        testExternalizable(new ExtWrapTagged(new ExtWrapMapPoly(h)), new ExtWrapTagged());
        h.put("key1", new SampleExtz("e", "f"));
        h.put("key2", new SampleExtz("c", "d"));
        h.put("key3", new SampleExtz("a", "b"));
        testExternalizable(new ExtWrapMap(h), new ExtWrapMap(String.class, SampleExtz.class), pf);
        testExternalizable(new ExtWrapTagged(new ExtWrapMap(h)), new ExtWrapTagged(), pf);

        HashMap j = new HashMap();
        j.put(new Integer(17), h);
        j.put(new Integer(-3), h);
        HashMap k = new HashMap();
        k.put("key", j);
        testExternalizable(new ExtWrapMap(k, new ExtWrapMap(Integer.class, new ExtWrapMap(String.class, SampleExtz.class))),
                new ExtWrapMap(String.class, new ExtWrapMap(Integer.class, new ExtWrapMap(String.class, SampleExtz.class))), pf);    //note: this example contains mixed hashtable types; would choke if we used a tagging wrapper

        OrderedMap m = new OrderedMap();
        m.put("a", "b");
        m.put("b", new Integer(17));
        m.put("c", new Short((short)-443));
        m.put("d", new SampleExtz("boris", "yeltsin"));
        m.put("e", new ExtWrapList(vs));
        testExternalizable(new ExtWrapMapPoly(m), new ExtWrapMapPoly(String.class, true), pf);
    }

    public static void testExternalizable (Object orig, Object template, PrototypeFactory pf, String failMessage) {
        if (failMessage == null)
            failMessage = "Serialization Failure";

        byte[] bytes;
        Object deser;

        print("");
        print("Original: " + printObj(orig));

        try {
            bytes = ExtUtil.serialize(orig);

            print("Serialized as:");
            print(ExtUtil.printBytes(bytes));

            if (template instanceof Class) {
                deser = ExtUtil.deserialize(bytes, (Class)template, pf);
            } else if (template instanceof ExternalizableWrapper) {
                deser = ExtUtil.read(new DataInputStream(new ByteArrayInputStream(bytes)), (ExternalizableWrapper)template, pf);
            } else {
                throw new ClassCastException();
            }

            print("Reconstituted: " + printObj(deser));

            if (ExtUtil.equals(orig, deser)) {
                print("SUCCESS");
            } else {
                print("FAILURE");
                fail(failMessage + ": Objects do not match");
            }
            print("---------------------------------------------");
        } catch (Exception e) {
            fail(failMessage + ": Exception! " + e.getClass().getName() + " " + e.getMessage());
        }
    }

    //for outside test suites to call
    public static void testExternalizable (Externalizable original, PrototypeFactory pf) {
        testExternalizable(original, pf, "Serialization failure for " + original.getClass().getName());
    }

    public static void testExternalizable (Externalizable original, PrototypeFactory pf, String failMessage) {
        testExternalizable(original, original.getClass(), pf, failMessage);
    }

    //for use inside this test suite
    public void testExternalizable (Object orig, Object template) {
        testExternalizable(orig, template, null);
    }

    public void testExternalizable (Object orig, Object template, PrototypeFactory pf) {
        testExternalizable(orig, template, pf, null);
    }

    public static String printObj (Object o) {
        o = ExtUtil.unwrap(o);

        if (o == null) {
            return "(null)";
        } else if (o instanceof List) {
            StringBuilder sb = new StringBuilder();
            sb.append("V[");
            List lo = (List) o;
            boolean first = true;
            for ( Object obj : lo ) {
                if ( !first ) {
                    sb.append(", ");
                }
                first = false;
                sb.append(printObj(obj));
            }
            sb.append("]");
            return sb.toString();
        } else if (o instanceof HashMap) {
            StringBuilder sb = new StringBuilder();
            sb.append((o instanceof OrderedMap ? "oH" : "H") + "[");
            for (Iterator e = ((HashMap)o).keySet().iterator(); e.hasNext(); ) {
                Object key = e.next();
                sb.append(printObj(key));
                sb.append("=>");
                sb.append(printObj(((HashMap)o).get(key)));
                if (e.hasNext())
                    sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        } else {
            return "{" + o.getClass().getName() + ":" + o.toString() + "}";
        }
    }

    private static void print (String s) {
        logger.info(s);
    }
}
