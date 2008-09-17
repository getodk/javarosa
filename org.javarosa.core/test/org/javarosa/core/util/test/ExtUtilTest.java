package org.javarosa.core.util.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.util.externalizable.ExtType;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.ExternalizableWrapper;

public class ExtUtilTest extends TestCase {
	public ExtUtilTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public ExtUtilTest(String name) {
		super(name);
	}
	
	public ExtUtilTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		
		aSuite.addTest(new ExtUtilTest("Externalizable Test", new TestMethod() {
			public void run (TestCase tc) {
				((ExtUtilTest)tc).doTest();
			}
		}));
			
		return aSuite;
	}
	
	public void testExternalizable (Object orig, Object template) {
		byte[] bytesOut;
		Object deser;
		Object base = orig;
		
		while (base instanceof ExternalizableWrapper) {
			base = ((ExternalizableWrapper)base).val;
		}
		
		System.out.println();
		System.out.println("Original: " + printObj(base));
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ExtUtil.write(new DataOutputStream(baos), orig);
			bytesOut = baos.toByteArray();

			System.out.println("Serialized as:");
			System.out.println(printBytes(bytesOut));
			
			ByteArrayInputStream bais = new ByteArrayInputStream(bytesOut);
			if (template instanceof Class) {
				deser = ExtUtil.read(new DataInputStream(bais), (Class)template);
			} else if (template instanceof ExternalizableWrapper) {
				deser = ExtUtil.read(new DataInputStream(bais), (ExternalizableWrapper)template);				
			} else {
				throw new ClassCastException();
			}
			
			System.out.println("Reconstituted: " + printObj(deser));
			
			if (orig == null ? deser == null : printObj(base).equals(printObj(deser))) {
				System.out.println("SUCCESS");
			} else {
				System.out.println("FAILURE");
				fail("Objects do not match");
			}
			System.out.println("---------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception! " + e.getClass().getName() + " " + e.getMessage());
		}
	}

	public static String printObj (Object o) {
		return o == null ? "(null)" : "{" + o.getClass().getName() + ":" + o.toString() + "}";
	}
	
	public static String printBytes (byte[] data) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < data.length; i++) {
			String hex = Integer.toHexString(data[i]);
			if (hex.length() == 1)
				hex = "0" + hex;
			else
				hex = hex.substring(hex.length() - 2);
			sb.append(hex);
			if (i < data.length - 1)
				sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public void doTest () {
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
		
		testExternalizable("string", new ExtType(String.class));
		testExternalizable(new ExtType("string"), String.class);
		
		testExternalizable(new ExtWrapNullable((String)null), new ExtWrapNullable(String.class));
		testExternalizable(new ExtWrapNullable("string"), new ExtWrapNullable(String.class));
		testExternalizable(new ExtWrapNullable((Integer)null), new ExtWrapNullable(Integer.class));
		testExternalizable(new ExtWrapNullable(new Integer(17)), new ExtWrapNullable(Integer.class));
		testExternalizable(new ExtWrapNullable((SampleExtz)null), new ExtWrapNullable(SampleExtz.class));
		testExternalizable(new ExtWrapNullable(new SampleExtz("hi", "there")), new ExtWrapNullable(SampleExtz.class));
	
		Vector v = new Vector();
		v.addElement(new Integer(27));
		v.addElement(new Integer(-73));
		v.addElement(new Integer(1024));
		v.addElement(new Integer(66066066));
		testExternalizable(new ExtWrapList(v), new ExtWrapList(Integer.class));
		
		testExternalizable(new ExtWrapNullable(new ExtWrapList(v)), new ExtWrapNullable(new ExtWrapList(Integer.class)));
		testExternalizable(new ExtWrapNullable((ExtWrapList)null), new ExtWrapNullable(new ExtWrapList(Integer.class)));
		testExternalizable(new ExtWrapList(v, new ExtWrapNullable()), new ExtWrapList(new ExtWrapNullable(Integer.class)));
		
		Vector w = new Vector();
		w.addElement(new SampleExtz("where", "is"));
		w.addElement(new SampleExtz("the", "beef"));
		testExternalizable(new ExtWrapList(w), new ExtWrapList(SampleExtz.class));
		
		testExternalizable(new ExtWrapList(new Vector()), new ExtWrapList(String.class));
		testExternalizable(new ExtWrapList(new Vector(), new ExtType(Integer.class)), new ExtWrapList(String.class));
		
		Vector x = new Vector();
		x.addElement(new Integer(-35));
		x.addElement(new Integer(-31415926));
		Vector y = new Vector();
		y.addElement(v);
		y.addElement(x);
		testExternalizable(new ExtWrapList(y, new ExtWrapList(Integer.class)), new ExtWrapList(new ExtWrapList(Integer.class)));
		testExternalizable(new ExtWrapList(new Vector(), new ExtWrapList(Integer.class)), new ExtWrapList(new ExtWrapList(Integer.class)));
		
		testExternalizable(new ExtWrapTagged("string"), new ExtWrapTagged(null, null));
		testExternalizable(new ExtWrapTagged(new Integer(5000)), new ExtWrapTagged(null, null));
		Vector proto = new Vector();
		proto.addElement(SampleExtz.class);
		testExternalizable(new ExtWrapTagged(new SampleExtz("bon", "jovi")), new ExtWrapTagged(null, proto));
		testExternalizable(new ExtWrapTagged(new ExtWrapList(v)), new ExtWrapTagged(null, null));
		
		testExternalizable(new ExtWrapTagged(new ExtWrapNullable("string")), new ExtWrapTagged(null, null));
		testExternalizable(new ExtWrapTagged(new ExtWrapNullable((String)null)), new ExtWrapTagged(null, null));
		testExternalizable(new ExtWrapTagged(new ExtWrapList(y, new ExtWrapList(Integer.class))), new ExtWrapTagged(null, null));
		
		Vector a = new Vector();
		a.addElement(new Integer(47));
		a.addElement("string");
		a.addElement(Boolean.FALSE);
		testExternalizable(new ExtWrapListPoly(a), new ExtWrapListPoly());
		testExternalizable(new ExtWrapTagged(new ExtWrapListPoly(a)), new ExtWrapTagged(null, null));
	}
}
