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

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.util.OrderedHashtable;
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
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class ExternalizableTest extends TestCase {
	public ExternalizableTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public ExternalizableTest(String name) {
		super(name);
	}
	
	public ExternalizableTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		
		aSuite.addTest(new ExternalizableTest("Externalizable Test", new TestMethod() {
			public void run (TestCase tc) {
				((ExternalizableTest)tc).doTests();
			}
		}));
			
		return aSuite;
	}

	public static void testExternalizable (Object orig, Object template, PrototypeFactory pf, TestCase tc, String failMessage) {
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
				tc.fail(failMessage + ": Objects do not match");
			}
			print("---------------------------------------------");
		} catch (Exception e) {
			tc.fail(failMessage + ": Exception! " + e.getClass().getName() + " " + e.getMessage());
		}
	}

	//for outside test suites to call
	public static void testExternalizable (Externalizable original, TestCase tc, PrototypeFactory pf) {
		testExternalizable(original, tc, pf, "Serialization failure for " + original.getClass().getName());
	}
	
	public static void testExternalizable (Externalizable original, TestCase tc, PrototypeFactory pf, String failMessage) {
		testExternalizable(original, original.getClass(), pf, tc, failMessage);
	}
	
	//for use inside this test suite
	public void testExternalizable (Object orig, Object template) {
		testExternalizable(orig, template, null);
	}	
	
	public void testExternalizable (Object orig, Object template, PrototypeFactory pf) {
		testExternalizable(orig, template, pf, this, null);
	}

	public static String printObj (Object o) {
		o = ExtUtil.unwrap(o);
		
		if (o == null) {
			return "(null)";
		} else if (o instanceof Vector) {
			StringBuffer sb = new StringBuffer();
			sb.append("V[");
			for (Enumeration e = ((Vector)o).elements(); e.hasMoreElements(); ) {
				sb.append(printObj(e.nextElement()));
				if (e.hasMoreElements())
					sb.append(", ");
			}
			sb.append("]");
			return sb.toString();
		} else if (o instanceof Hashtable) {
			StringBuffer sb = new StringBuffer();
			sb.append((o instanceof OrderedHashtable ? "oH" : "H") + "[");
			for (Enumeration e = ((Hashtable)o).keys(); e.hasMoreElements(); ) {
				Object key = e.nextElement();
				sb.append(printObj(key));
				sb.append("=>");
				sb.append(printObj(((Hashtable)o).get(key)));
				if (e.hasMoreElements())
					sb.append(", ");
			}
			sb.append("]");
			return sb.toString();
		} else {
			return "{" + o.getClass().getName() + ":" + o.toString() + "}";
		}
	}
	
	private static void print (String s) {
		//#if javarosa.dev.serializationtest.verbose
		System.out.println(s);
		//#endif
	}
	
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

		//vectors of base types
		Vector v = new Vector();
		v.addElement(new Integer(27));
		v.addElement(new Integer(-73));
		v.addElement(new Integer(1024));
		v.addElement(new Integer(66066066));
		testExternalizable(new ExtWrapList(v), new ExtWrapList(Integer.class));

		Vector vs = new Vector();
		vs.addElement("alpha");
		vs.addElement("beta");
		vs.addElement("gamma");
		testExternalizable(new ExtWrapList(vs), new ExtWrapList(String.class));	

		Vector w = new Vector();
		w.addElement(new SampleExtz("where", "is"));
		w.addElement(new SampleExtz("the", "beef"));
		testExternalizable(new ExtWrapList(w), new ExtWrapList(SampleExtz.class));
		
		//nullable vectors; vectors of nullables (no practical use)
		testExternalizable(new ExtWrapNullable(new ExtWrapList(v)), new ExtWrapNullable(new ExtWrapList(Integer.class)));
		testExternalizable(new ExtWrapNullable((ExtWrapList)null), new ExtWrapNullable(new ExtWrapList(Integer.class)));
		testExternalizable(new ExtWrapList(v, new ExtWrapNullable()), new ExtWrapList(new ExtWrapNullable(Integer.class)));

		//empty vectors (base types)
		testExternalizable(new ExtWrapList(new Vector()), new ExtWrapList(String.class));
		testExternalizable(new ExtWrapList(new Vector(), new ExtWrapBase(Integer.class)), new ExtWrapList(String.class)); //sub-types don't matter for empties
		
		//vectors of vectors (including empties)
		Vector x = new Vector();
		x.addElement(new Integer(-35));
		x.addElement(new Integer(-31415926));
		Vector y = new Vector();
		y.addElement(v);
		y.addElement(x);
		y.addElement(new Vector());
		testExternalizable(new ExtWrapList(y, new ExtWrapList()), new ExtWrapList(new ExtWrapList(Integer.class))); //risky to not specify 'leaf' type (Integer), but works in limited situations
		testExternalizable(new ExtWrapList(new Vector(), new ExtWrapList()), new ExtWrapList(new ExtWrapList(Integer.class))); //same as above

		//tagged base types
		testExternalizable(new ExtWrapTagged("string"), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new Integer(5000)), new ExtWrapTagged());
		//tagged custom type
		PrototypeFactory pf = new PrototypeFactory();
		pf.addClass(SampleExtz.class);
		testExternalizable(new ExtWrapTagged(new SampleExtz("bon", "jovi")), new ExtWrapTagged(), pf);
		//tagged vector (base type)
		testExternalizable(new ExtWrapTagged(new ExtWrapList(v)), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new ExtWrapList(w)), new ExtWrapTagged(), pf);
		//tagged nullables and compound vectors
		testExternalizable(new ExtWrapTagged(new ExtWrapNullable("string")), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new ExtWrapNullable((String)null)), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new ExtWrapList(y, new ExtWrapList(Integer.class))), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new ExtWrapList(new Vector(), new ExtWrapList(Integer.class))), new ExtWrapTagged());
		
		//polymorphic vectors
		Vector a = new Vector();
		a.addElement(new Integer(47));
		a.addElement("string");
		a.addElement(Boolean.FALSE);
		a.addElement(new SampleExtz("hello", "dolly"));
		testExternalizable(new ExtWrapListPoly(a), new ExtWrapListPoly(), pf);
		testExternalizable(new ExtWrapTagged(new ExtWrapListPoly(a)), new ExtWrapTagged(), pf);
		//polymorphic vector with complex sub-types
		a.addElement(new ExtWrapList(y, new ExtWrapList(Integer.class))); //note: must manually wrap children in polymorphic lists
		testExternalizable(new ExtWrapListPoly(a), new ExtWrapListPoly(), pf);
		testExternalizable(new ExtWrapListPoly(new Vector()), new ExtWrapListPoly());
		
		//hashtables
		OrderedHashtable oh = new OrderedHashtable();
		testExternalizable(new ExtWrapMap(oh), new ExtWrapMap(String.class, Integer.class, true));
		testExternalizable(new ExtWrapMapPoly(oh), new ExtWrapMapPoly(Date.class, true));
		testExternalizable(new ExtWrapTagged(new ExtWrapMap(oh)), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new ExtWrapMapPoly(oh)), new ExtWrapTagged());
		oh.put("key1", new SampleExtz("a", "b"));
		oh.put("key2", new SampleExtz("c", "d"));
		oh.put("key3", new SampleExtz("e", "f"));
		testExternalizable(new ExtWrapMap(oh), new ExtWrapMap(String.class, SampleExtz.class, true), pf);		
		testExternalizable(new ExtWrapTagged(new ExtWrapMap(oh)), new ExtWrapTagged(), pf);		
		
		Hashtable h = new Hashtable();
		testExternalizable(new ExtWrapMap(h), new ExtWrapMap(String.class, Integer.class));
		testExternalizable(new ExtWrapMapPoly(h), new ExtWrapMapPoly(Date.class));
		testExternalizable(new ExtWrapTagged(new ExtWrapMap(h)), new ExtWrapTagged());
		testExternalizable(new ExtWrapTagged(new ExtWrapMapPoly(h)), new ExtWrapTagged());
		h.put("key1", new SampleExtz("e", "f"));
		h.put("key2", new SampleExtz("c", "d"));
		h.put("key3", new SampleExtz("a", "b"));
		testExternalizable(new ExtWrapMap(h), new ExtWrapMap(String.class, SampleExtz.class), pf);		
		testExternalizable(new ExtWrapTagged(new ExtWrapMap(h)), new ExtWrapTagged(), pf);		
		
		Hashtable j = new Hashtable();
		j.put(new Integer(17), h);
		j.put(new Integer(-3), h);
		Hashtable k = new Hashtable();
		k.put("key", j);
		testExternalizable(new ExtWrapMap(k, new ExtWrapMap(Integer.class, new ExtWrapMap(String.class, SampleExtz.class))),
				new ExtWrapMap(String.class, new ExtWrapMap(Integer.class, new ExtWrapMap(String.class, SampleExtz.class))), pf);	//note: this example contains mixed hashtable types; would choke if we used a tagging wrapper
		
		OrderedHashtable m = new OrderedHashtable();
		m.put("a", "b");
		m.put("b", new Integer(17));
		m.put("c", new Short((short)-443));
		m.put("d", new SampleExtz("boris", "yeltsin"));
		m.put("e", new ExtWrapList(vs));
		testExternalizable(new ExtWrapMapPoly(m), new ExtWrapMapPoly(String.class, true), pf);
	}
}
